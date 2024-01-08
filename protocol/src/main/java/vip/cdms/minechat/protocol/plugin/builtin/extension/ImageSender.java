package vip.cdms.minechat.protocol.plugin.builtin.extension;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.JsonArray;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.show.Toast;
import vip.cdms.minechat.protocol.dataexchange.client.ExtensionClient;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.ProtocolExtension;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StringCommandHelper;

public class ImageSender extends ProtocolExtension {
    private final int dp;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    JLanguage lang = new JLanguage();
    /** @noinspection unchecked*/
    public ImageSender(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
        lang.set(activity);
        setPluginIcon("iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAMAAAC67D+PAAAAAXNSR0IArs4c6QAAACdQTFRFZJXsHh4fSXpBXUs5W4vhdlc4t8z33cyohHJg093y5e39UD0pcp1rHmTAOwAAAEBJREFUCJlNx0EOgDAIBVHyp1Bovf95xcYY32rG4rWWRcjRMxbeoKozMztVZVe6DyBk6hpIyE7BZts8YGI/43MDcggB093j0MYAAAAASUVORK5CYII=");
        setPluginTitle(lang.add("Image Sender", "图片发送"));
        setPluginSummary(lang.add("[built-in] Send pictures in the game!", "[内置] 在游戏里发送图片!"));

        dp = MathUtils.dp2px(activity, 1);
        if (!activity.getClass().getName().equals("vip.cdms.minechat.activity.chat.ChatActivity")) return;
        tryCatch(() -> imagePickerLauncher = (ActivityResultLauncher<Intent>) ReflectionUtils.invoke("registerForActivityResult", ReflectionUtils.D_U_P_T, getActivity(),
                ActivityResultContract.class, new ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback.class, (ActivityResultCallback<ActivityResult>) result -> tryCatch(() -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    Intent data = result.getData();
                    if (data == null) return;
                    Uri selectedImageUri = data.getData();
                    selectedImage(MediaStore.Images.Media.getBitmap(
                            getActivity().getContentResolver(),
                            selectedImageUri
                    ));
                })));
    }

//    @Override
//    public void openPluginSetting() {
//    }






    void sendBase64(int quality) {
        pickImage(() -> tryCatch(() -> {
            String base64;

            {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
                byte[] imageByte = outputStream.toByteArray();
                base64 = Base64.encodeToString(imageByte, Base64.DEFAULT);
            }

            {
                Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
                deflater.setInput(base64.getBytes());
                deflater.finish();
                final byte[] bytes = new byte[256];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
                while (!deflater.finished()) {
                    int length = deflater.deflate(bytes);
                    outputStream.write(bytes, 0, length);
                }
                deflater.end();
                base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_PADDING);
            }

            getClient().sendMessage("IMGS|" + base64);
        }));
    }
    static final Pattern BASE64_MSG = Pattern.compile("^(.+)IMGS\\|([\\w\\W]*)$");
    @Override
    public CharSequence onPrint(CharSequence message) {
        Matcher matcher = BASE64_MSG.matcher(message.toString());
        if (matcher.find()) {
            message = matcher.group(1) + "[BASE64]";
            TimeUtils.setTimeout(() -> tryCatch(() -> {
                String base64 = matcher.group(2);

                {
                    byte[] decode = Base64.decode(base64, Base64.NO_PADDING);
                    Inflater inflater = new Inflater();
                    inflater.setInput(decode);
                    final byte[] bytes = new byte[256];
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
                    while (!inflater.finished()) {
                        int length = inflater.inflate(bytes);
                        outputStream.write(bytes, 0, length);
                    }
                    inflater.end();
                    base64 = outputStream.toString();
                }

                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Drawable drawable = new BitmapDrawable(getActivity().getResources(), bitmap);

                int width = drawable.getMinimumWidth();
                int height = drawable.getMinimumHeight();

                int maxWidth = getActivity().getResources().getDisplayMetrics().widthPixels - 16 * dp;
                if (width > maxWidth) {
                    height = (int) (height * ((float) maxWidth / width));
                    width = maxWidth;
                }

                drawable.setBounds(0, 0, width, height);

                SpannableStringBuilder ssb = new SpannableStringBuilder("[IMGS]");
                ssb.setSpan(new ImageSpan(drawable), 0, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                getActivity().runOnUiThread(() -> getClient().print(ssb));
            }), 0);
        }
        return message;
    }






    record Color(int color) {
        public int red() {
            return android.graphics.Color.red(color);
        }
        public int green() {
            return android.graphics.Color.green(color);
        }
        public int blue() {
            return android.graphics.Color.blue(color);
        }
    }
    private interface ColorDifference {
        double getColorDifference(Color c1, Color c2);
    }
    private final ArrayList<ColorDifference> colorDifferences = new ArrayList<>() {{
        add((c1, c2) ->
                Math.pow(c1.red() - c2.red(), 2)
                        + Math.pow(c1.green() - c2.green(), 2)
                        + Math.pow(c1.blue() - c2.blue(), 2));
        add((c1, c2) ->
                1 - (255 - (Math.abs(c1.red() - c2.red()) * 255 * 0.297 + Math.abs(c1.green() - c2.green()) * 255 * 0.593 + Math.abs(c1.blue() - c2.blue()) * 255 * 11.0 / 100)) / 255);
        add(new ColorDifference() {
            public static int[] rgb2lab(int R, int G, int B) {
                //http://www.brucelindbloom.com

                float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
                float Ls, as, bs;
                float eps = 216.f / 24389.f;
                float k = 24389.f / 27.f;

                float Xr = 0.964221f;  // reference white D50
                float Yr = 1.0f;
                float Zr = 0.825211f;

                // RGB to XYZ
                r = R / 255.f; //R 0..1
                g = G / 255.f; //G 0..1
                b = B / 255.f; //B 0..1

                // assuming sRGB (D65)
                if (r <= 0.04045)
                    r = r / 12;
                else
                    r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

                if (g <= 0.04045)
                    g = g / 12;
                else
                    g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

                if (b <= 0.04045)
                    b = b / 12;
                else
                    b = (float) Math.pow((b + 0.055) / 1.055, 2.4);


                X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
                Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
                Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

                // XYZ to Lab
                xr = X / Xr;
                yr = Y / Yr;
                zr = Z / Zr;

                if (xr > eps)
                    fx = (float) Math.pow(xr, 1 / 3.);
                else
                    fx = (float) ((k * xr + 16.) / 116.);

                if (yr > eps)
                    fy = (float) Math.pow(yr, 1 / 3.);
                else
                    fy = (float) ((k * yr + 16.) / 116.);

                if (zr > eps)
                    fz = (float) Math.pow(zr, 1 / 3.);
                else
                    fz = (float) ((k * zr + 16.) / 116);

                Ls = (116 * fy) - 16;
                as = 500 * (fx - fy);
                bs = 200 * (fy - fz);

                int[] lab = new int[3];
                lab[0] = (int) (2.55 * Ls + .5);
                lab[1] = (int) (as + .5);
                lab[2] = (int) (bs + .5);
                return lab;
            }
//        /**
//         * Computes the difference between two RGB colors by converting them to the L*a*b scale and
//         * comparing them using the CIE76 algorithm { http://en.wikipedia.org/wiki/Color_difference#CIE76 }
//         */
            @Override
            public double getColorDifference(Color a, Color b) {
                int r1, g1, b1, r2, g2, b2;
                r1 = a.red();
                g1 = a.green();
                b1 = a.blue();
                r2 = b.red();
                g2 = b.green();
                b2 = b.blue();
                int[] lab1 = rgb2lab(r1, g1, b1);
                int[] lab2 = rgb2lab(r2, g2, b2);
                return Math.sqrt(Math.pow(lab2[0] - lab1[0], 2) + Math.pow(lab2[1] - lab1[1], 2) + Math.pow(lab2[2] - lab1[2], 2));
            }
        });
        add((c1, c2) -> Math.sqrt(
                (2 + (c1.red() + c2.red()) / 256.0) * Math.pow(c1.red() - c2.red(), 2) +
                        4.0 * Math.pow(c1.green() - c2.green(), 2) +
                        (2 + (255 - (c1.red() + c2.red()) / 256.0)) * Math.pow(c1.blue() - c2.blue(), 2)));
    }};

    static final HashMap<String, Color> MC_COLORS = new HashMap<>();
    void sendMCFormat(int accuracy, ColorDifference colorDifference, String fill, Consumer<String> execute, String format,  boolean split, int delay) {
        if (MC_COLORS.isEmpty())
            for (Map.Entry<String, String> entry : MCTextParser.COLORS.entrySet())
                MC_COLORS.put(entry.getKey(), new Color(android.graphics.Color.parseColor("#FF" + entry.getValue())));

        Function<Color, String> getClosestColor = target -> {
            double minDistance = Double.MAX_VALUE;
            String closestColorStr = "";
            for (Map.Entry<String, Color> entry : MC_COLORS.entrySet()) {
                String colorStr = entry.getKey();
                Color color = entry.getValue();
                double distance = colorDifference.getColorDifference(target, color);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestColorStr = colorStr;
                }
            }
            return closestColorStr;
        };

        pickImage(() -> {
            StringBuilder sb = new StringBuilder();
            int width = selectedImage.getWidth();
            int height = selectedImage.getHeight();
            String lastColor = "";
            for (int y = 0; y < height; y += accuracy) {
                sb.append("\n");
                for (int x = 0; x < width; x += accuracy) {
                    int color = selectedImage.getPixel(x, y);
                    String colour = getClosestColor.apply(new Color(color));
                    if (colour.equals(lastColor))
                        colour = "";
                    else
                        lastColor = colour;
                    sb.append(colour)
                            .append(fill);
                }
            }

            if (!split) {
                execute.accept(String.format(format, sb));
                return;
            }

            String[] lines = sb.toString().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.trim().isEmpty()) continue;
                int finalI = i;
                TimeUtils.setTimeout(() -> {
                    execute.accept(String.format(format, line));
                    runOnUiThread(() ->
                            new Toast()
                                    .setTitle(getPluginTitle().toString())
                                    .setMessage((finalI + 1) + " / " + lines.length)
                                    .show(getActivity()));
                }, i * delay);
            }
        });
    }






    Runnable selectCallback;
    Bitmap selectedImage;
    void pickImage(Runnable selectCallback) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
        this.selectCallback = selectCallback;
    }
    void selectedImage(Bitmap bitmap) {
        selectedImage = bitmap;
        selectCallback.run();
        selectCallback = null;
    }

    static class CommandHelper extends StringCommandHelper.ListenerHelper {
        static final String prefix = "^(?:imageSender|images|imgs) ?";
        @OnRegex(regex = prefix + "(help|\\?)?$")
        public void help(JLanguage lang, ExtensionClient client) {
            client.print(MCTextParser.easyFormat(lang.add("&2--- &aImage Sender &2---", "&2--- &a图片发送 &2---").toString()));
            help(client, "/imgs [help|?]");
            help(client, "/imgs base64 [quality:num#0]");
            help(client, "/imgs mc [acc:num#50] [algo:num#1] [fill:str#臵] [split:bool#1] [delay:num#1000]");
        }
        public void help(ExtensionClient client, String text) {
            client.print(text.replaceAll("([\\[\\]<>|#:])", MCTextParser.easyFormat("&7&m$1&r")));
        }

        @OnRegex(regex = prefix + "base64 ?([0-9]+)?$")
        public void base64(ImageSender imageSender, Integer quality) {
            imageSender.sendBase64(quality == null ? 0 : quality);
        }

        @OnRegex(regex = prefix + "mc ?([0-9]+)? ?([1-4])? ?(.)? ?(true|false|1|0)? ?([0-9]+)?$")
        public void mc(
                ExtensionClient client,
                ImageSender imageSender,
                Integer accuracy,
                Integer algorithm,
                String fill,
                Boolean split,
                Integer delay
        ) {
            imageSender.sendMCFormat(
                    accuracy == null ? 50 : accuracy,
                    imageSender.colorDifferences.get(algorithm == null ? 0 : algorithm - 1),
                    fill == null ? "臵" : fill,
                    client::sendMessage,
                    "%s",
                    split != null && split,
                    delay == null ? 1000 : delay
            );
        }
    }
    CommandHelper commandHelper;
    StringCommandHelper.ListenerHost commandHost;
    @Override
    public void onConnected(ProtocolProvider protocolProvider, ExtensionClient client) {
        super.onConnected(protocolProvider, client);
        commandHelper = new CommandHelper();
        commandHost = StringCommandHelper.easyTransfer(commandHelper,
                getExceptionHandler(), getActivity(), client, lang, this);
    }
    @Override
    public boolean onCommand(String command) {
        return commandHost.onCommand(command);
    }

    @Override
    public void onExtensionClick(View view) {
        new DialogBuilder(getActivity())
                .setTitle(getPluginTitle())
                .setContent(lang.add("Please select a sending method", "请选择发送方式"))
                .addAction("Base64", v -> new CustomFormBuilder()
                        .setTitle(getPluginTitle())
                        .addSlider(lang.add("Quality", "质量"), 0, 100, null, 0)
                        .setCallback(callback -> {
                            if (callback.isCancel()) {
                                onExtensionClick(view);
                                return;
                            }
                            sendBase64(callback.responseData().get(0).getAsInt());
                        })
                        .show(getActivity()))
                .addAction(lang.add("MC formatted text", "MC格式文本"), v -> {
                    SharedPreferences preferences = getSharedPreferences();
                    new CustomFormBuilder()
                            .setTitle(getPluginTitle())
                            .addSlider(lang.add("Scanning accuracy", "扫描精度"), 1, 100, null, preferences.getInt("accuracy", 50))
                            .addDropdown(lang.add("Color difference algorithm", "色彩差别算法"), new CharSequence[]{
                                    "1",
                                    "2",
                                    "3",
                                    "4"
                            }, preferences.getInt("algorithm", 0))
                            .addInput(lang.add("Filled text", "填充文本"), null, preferences.getString("fill", "臵"))
                            .addDropdown(lang.add("How to send it", "发送形式"), new CharSequence[]{
                                    lang.add("Send message(s)", "发送消息"),
                                    lang.add("Execute command(s)", "执行命令")
                            }, preferences.getInt("way", 0))
                            .addInput(lang.add("Text format", "发送格式"), "%s", preferences.getString("format", "%s"))
                            .addSwitch(lang.add("Send in stripes", "分条发送"), preferences.getBoolean("split", true))
                            .addInput(lang.add("Delay", "发送间延迟") + " (ms)", null, preferences.getString("delay", "1000"))
                            .setCallback(callback -> {
                                if (callback.isCancel()) {
                                    onExtensionClick(view);
                                    return;
                                }
                                JsonArray data = callback.responseData();

                                int accuracy = data.get(0).getAsInt();
                                int algorithm = data.get(1).getAsInt();
                                String fill = data.get(2).getAsString();
                                int way = data.get(3).getAsInt();
                                String format = data.get(4).getAsString();
                                boolean split = data.get(5).getAsBoolean();
                                String delay = data.get(6).getAsString();

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("accuracy", accuracy);
                                editor.putInt("algorithm", algorithm);
                                editor.putString("fill", fill);
                                editor.putInt("way", way);
                                editor.putString("format", format);
                                editor.putBoolean("split", split);
                                editor.putString("delay", delay);
                                editor.apply();

                                sendMCFormat(
                                        accuracy,
                                        colorDifferences.get(algorithm),
                                        fill,
                                        switch (way) {
                                            case 0 -> (Consumer<String>) s -> getClient().sendMessage(s);
                                            case 1 -> (Consumer<String>) s -> getClient().executeCommand(s);
                                            default -> null;
                                        },
                                        format,
                                        split,
                                        ExceptionHandler.ror(() ->
                                                Integer.parseInt(delay), 1000)
                                );
                            })
                            .show(getActivity());
                })
                .show();
    }
}
