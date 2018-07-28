
package android.graphics;

import android.os.AsyncTask;

import android.content.Context;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.view.PinkViewInvalidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;

public class PinkVision {

    private static boolean DEBUG = false;

    private static final String API_KEY = "AIzaSyBvgtvwYFqQmy_lSqpqPkMZKaMkIEqx244";
    private static final int CONN_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 15;
    private static final String B_STATS_VERSION = "0.1";
    private static final int MAX_DIMENSION = 640;

    private static final Map<String, Integer> SAFESEARCH_LEVEL;
    private static final String FILTER_LEVEL = "POSSIBLE";
    private static final int FILTER_LEVEL_INT;

    private static final String LOADING_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAIAAAC3LO29AAAAAXNSR0IArs4c6QAAAAlwSFlzAAALEwAACxMBAJqcGAAAA6ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IlhNUCBDb3JlIDUuNC4wIj4KICAgPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICAgICAgPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIKICAgICAgICAgICAgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIgogICAgICAgICAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgICAgICAgICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iPgogICAgICAgICA8eG1wOk1vZGlmeURhdGU+MjAxOC0wNS0wNlQxNTowNTo1NDwveG1wOk1vZGlmeURhdGU+CiAgICAgICAgIDx4bXA6Q3JlYXRvclRvb2w+UGl4ZWxtYXRvciAzLjc8L3htcDpDcmVhdG9yVG9vbD4KICAgICAgICAgPHRpZmY6T3JpZW50YXRpb24+MTwvdGlmZjpPcmllbnRhdGlvbj4KICAgICAgICAgPHRpZmY6Q29tcHJlc3Npb24+NTwvdGlmZjpDb21wcmVzc2lvbj4KICAgICAgICAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICAgICAgICAgPHRpZmY6WVJlc29sdXRpb24+NzI8L3RpZmY6WVJlc29sdXRpb24+CiAgICAgICAgIDx0aWZmOlhSZXNvbHV0aW9uPjcyPC90aWZmOlhSZXNvbHV0aW9uPgogICAgICAgICA8ZXhpZjpQaXhlbFhEaW1lbnNpb24+NzU8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpDb2xvclNwYWNlPjE8L2V4aWY6Q29sb3JTcGFjZT4KICAgICAgICAgPGV4aWY6UGl4ZWxZRGltZW5zaW9uPjc1PC9leGlmOlBpeGVsWURpbWVuc2lvbj4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgIDwvcmRmOlJERj4KPC94OnhtcG1ldGE+ChYq6nIAAATKSURBVHgB7VnJK3ZRGPe+5imEMmQsETJTiGSjlGRKWSjZYGHhj1CirKRY2VAWEjKsbIgUhRAWkiFjyTz7fm/XPd/93nuG61vdq/Mu3Oc+0/t7zu+c55zzsn19fbn86o/9V1fnKE5WaH2KJYeSQ/OPgJyl5udIhFByKBoh89slh+bnSIRQcigaIfPbJYfm50iEUHIoGiHz2yWH5udIhFByKBoh89slh+bnSIRQcigaIfPbfz+HbnoS3t7eNjY2lpeXNzc33d3ds7Ozc3Nzk5OTbTab3tmg5uLiYnV1FTmPj4+joqLy8/OzsrJCQ0MNhuvdPj8/t7e3V1ZW1tbW3t/fMzIy8vLy0tLS3Nx0FeE/M+Tz+PjY39+fk5ODwrRJvb29S0pKRkdHkZc4GxSAo6WlJSIiQpsQcmRkZFtb2+7ursE8xO3j42NkZKS4uBiotDk9PDzAxMDAwNPTE3GG4EJeAAVh2hi9XFdXd35+TkKEAsYrODhYn4doQOPg4KAwD3E4PT2tqqoi4VShtLR0b2+PhHxXuLW1FR8fTw1wUmKCGSyys7PTKZb12tPTQwBxBJQHllhJtPqEhAQyOxwV3t7eGoxUstTX12OqcKDANDExQVkSWhQaGYtiZmaGnxCLrbq6WhMkEAsKCu7v75HTUWFvb6/A/V8zWs709DQHEFJj0f8bJHhDP0MX4OQcHx8XpNCZ+/r6HBU+PDykpKTorAJFRUUFB83Y2JggnmaenJxk5USHKysrowXxdGiw6Dou6ODGpxPJh/5xcnLCAtTc3Ew8jQutra2shIeHh4GBgcZTKZ6Y/Nii7Ovr65jiPw2+vr4+OjqiRgHlzs4O1cRXopmzHFDhzc0Ny8rSY2NHTvvZ2RnLg6/HYqM6oAk9Pz9TTXylsg6pPnd3d1S9UImThj06Olrop3dAswkICNDroXF1dfX19aWa+Ep/f3/WsSkoKIgfy7LiXGHHcsRpgOXB0oeFhcXExFCtQJmenk418ZVAwnKIjY39jyOel5dXamqqy8vLy482QwVEQ0MDqytAPzc3x2KDVYPdbp+fn+fkrKmpYcWy9NgSsRQd++HQ0BDLiapHj1pYWOCgwagVFRVRY1lKHLVeX185OVH/T3s+jq9I6KgQgMrLy1nfrde3t7dzoCimxcVF46sRKxC3BH5ObInYTvRgWJrKykoQiJyOCvHB5obbB8tbq8fBVzkNKYGcv5gaWAnaWKqMK8Lw8DAnDzGho+KkQU3ipCwsLMQeoQR+V4gXnKexujjrx9PTs6Ojg3+2ImgUYWpqCodgp6/XviYmJs7OzjpFcV4xuJhBnNYI/I2NjVdXVyTJ3woVFQ6cYCkkJITgQA9Az21qalpaWiJhxgUMXFdXV2ZmpvY65+Pjg4Nod3f35eWl8VTEE0sAZeDOqeUDzba2thZNjrgpgg0PUgwRcE/B7QNfj80NG0NSUhL/mkcCWQK6yP7+/sHBAY7Bfn5+cXFx4BYdi+VvRA94AKnMRpSH6RAeHq4PpFeo97Ou5vf/EiUrtO7sVJFLDtWRsO5Tcmhd7lTkkkN1JKz7lBxalzsVueRQHQnrPiWH1uVORS45VEfCuk/JoXW5U5FLDtWRsO5Tcmhd7lTkkkN1JKz7lBxalzsV+R9u7H2TKnBpeQAAAABJRU5ErkJggg==";
    private static final byte[] LOADING_IMAGE_BYTE = Base64.decode(LOADING_IMAGE_BASE64, Base64.NO_WRAP);
    private static final Bitmap LOADING_IMAGE = BitmapFactory.decodeByteArray(LOADING_IMAGE_BYTE, 0,
            LOADING_IMAGE_BYTE.length);

    private static final String PINKDROID_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAIAAAD/gAIDAAAABGdBTUEAALGPC/xhBQAAAAlwSFlzAAALEwAACxMBAJqcGAAABCRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IlhNUCBDb3JlIDUuNC4wIj4KICAgPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICAgICAgPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIKICAgICAgICAgICAgeG1sbnM6dGlmZj0iaHR0cDovL25zLmFkb2JlLmNvbS90aWZmLzEuMC8iCiAgICAgICAgICAgIHhtbG5zOmV4aWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20vZXhpZi8xLjAvIgogICAgICAgICAgICB4bWxuczpkYz0iaHR0cDovL3B1cmwub3JnL2RjL2VsZW1lbnRzLzEuMS8iCiAgICAgICAgICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyI+CiAgICAgICAgIDx0aWZmOlJlc29sdXRpb25Vbml0PjI8L3RpZmY6UmVzb2x1dGlvblVuaXQ+CiAgICAgICAgIDx0aWZmOkNvbXByZXNzaW9uPjU8L3RpZmY6Q29tcHJlc3Npb24+CiAgICAgICAgIDx0aWZmOlhSZXNvbHV0aW9uPjcyPC90aWZmOlhSZXNvbHV0aW9uPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICAgICA8dGlmZjpZUmVzb2x1dGlvbj43MjwvdGlmZjpZUmVzb2x1dGlvbj4KICAgICAgICAgPGV4aWY6UGl4ZWxYRGltZW5zaW9uPjEwMDwvZXhpZjpQaXhlbFhEaW1lbnNpb24+CiAgICAgICAgIDxleGlmOkNvbG9yU3BhY2U+MTwvZXhpZjpDb2xvclNwYWNlPgogICAgICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MTAwPC9leGlmOlBpeGVsWURpbWVuc2lvbj4KICAg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";
    private static final byte[] PINKDROID_BYTE = Base64.decode(PINKDROID_BASE64, Base64.NO_WRAP);
    private static final Bitmap PINKDROID = BitmapFactory.decodeByteArray(PINKDROID_BYTE, 0, PINKDROID_BYTE.length);

    private PinkViewInvalidator pinkViewInvalidator = PinkViewInvalidator.getInstance();

    private static AtomicInteger PinkAPICounter = new AtomicInteger(0);
    private static AtomicInteger PinkBlockCounter = new AtomicInteger(0);

    public static Bitmap createLoadingImage(@NonNull Bitmap bitmap) {
        // Create Loading bitmap image which has same size
        return createLoadingImage(bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap createPinkDroid(@NonNull Bitmap bitmap) {
        // Create PinkDroid bitmap image which has same size

        int cnt = PinkBlockCounter.addAndGet(1);
        Log.i("PinkCounter", "Block: " + String.valueOf(cnt));
        return createPinkDroid(bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap createLoadingImage(int width, int height) {
        // Create Loading image by using width and height
        if (width < 75 || height < 75) {
            return createImage(width, height, 0);
        }

        Bitmap outputimage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputimage);
        can.drawARGB(255, 255, 255, 255); // This represents White color
        can.drawBitmap_Backend(LOADING_IMAGE, (width - 75) / 2, (height - 75) / 2, null);
        return outputimage;

    }

    public static Bitmap createPinkDroid(int width, int height) {
        // Create PinkDroid image by using width and height

        if (width < 100 || height < 100) {
            return createImage(width, height, 0);
        }

        Bitmap outputimage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputimage);
        can.drawARGB(255, 255, 255, 255); // This represents White color
        can.drawBitmap_Backend(PINKDROID, (width - 100) / 2, (height - 100) / 2, null);
        return outputimage;

    }

    private static Bitmap createImage(int width, int height, int color) {
        // Create Emtpy Image
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

    // Values for parsing CloudVision response
    static {
        SAFESEARCH_LEVEL = new HashMap<String, Integer>();
        SAFESEARCH_LEVEL.put("VERY_UNLIKELY", 0);
        SAFESEARCH_LEVEL.put("UNLIKELY", 1);
        SAFESEARCH_LEVEL.put("POSSIBLE", 2);
        SAFESEARCH_LEVEL.put("LIKELY", 3);
        SAFESEARCH_LEVEL.put("VERY_LIKELY", 4);
        SAFESEARCH_LEVEL.put("UNKNOWN", 5);
        FILTER_LEVEL_INT = SAFESEARCH_LEVEL.get(FILTER_LEVEL);
    }

    // Cache Result
    public enum CacheResult {
        SAFE, UNSAFE, PROCESSING, MISS,
    }

    // Image Result Cache (It shoul be concurrent hash map)
    private static Map<String, CacheResult> IMAGE_CACHE = new ConcurrentHashMap<String, CacheResult>();

    private static String generateHash(@NonNull Bitmap bitmap) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(getImageBytes(bitmap));
            byte byteData[] = digest.digest();
            return Base64.encodeToString(byteData, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException ex) {
            return "ERROR!";
        }
    }

    public CacheResult getCacheResult(@NonNull Bitmap bitmap) {
        String encoded = generateHash(bitmap);
        if (IMAGE_CACHE.containsKey(encoded)) {
            return IMAGE_CACHE.get(encoded);
        } else {
            return CacheResult.MISS;
        }
    }

    private void setCacheResult(@NonNull Bitmap bitmap, CacheResult cacheResult) {
        IMAGE_CACHE.put(generateHash(bitmap), cacheResult);
    }

    private static PinkVision instance = new PinkVision();

    public static PinkVision getInstance() {

        return instance;
    }

    private HttpsURLConnection getConnection() throws IOException {
        Log.i("PinkVision", "new getConnection");
        URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setConnectTimeout(CONN_TIMEOUT * 1000);
        conn.setReadTimeout(READ_TIMEOUT * 1000);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Accept", "application/json");
        conn.addRequestProperty("Connection", "close");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "PinkVision/" + B_STATS_VERSION);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        return conn;
    }

    private HttpURLConnection getConnection_debug() throws IOException {
        Log.i("PinkVision", "new DEBUG getConnection");
        URL url = new URL("http://143.248.41.4:5000");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONN_TIMEOUT * 1000);
        conn.setReadTimeout(READ_TIMEOUT * 1000);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Accept", "application/json");
        conn.addRequestProperty("Connection", "close");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "PinkVision/" + B_STATS_VERSION);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        return conn;
    }

    private JSONObject generateBody(@NonNull Bitmap bitmap) throws JSONException {

        Bitmap scaleDownBitmap = scaleBitmapDown(bitmap);
        String encoded = getBase64(scaleDownBitmap);
        JSONObject image = new JSONObject();
        image.put("content", encoded);
        JSONObject safesearch = new JSONObject();
        safesearch.put("type", "SAFE_SEARCH_DETECTION");
        JSONArray features = new JSONArray();
        features.put(safesearch);

        JSONObject request = new JSONObject();
        request.put("image", image);
        request.put("features", features);

        JSONArray requests = new JSONArray();
        requests.put(request);

        JSONObject final_requests = new JSONObject();
        final_requests.put("requests", requests);

        return final_requests;
    }

    private static byte[] getImageBytes(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static String getBase64(@NonNull Bitmap bitmap) {
        byte[] imageBytes = getImageBytes(bitmap);

        String encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return encoded;

    }

    private static Bitmap scaleBitmapDown(@NonNull Bitmap bitmap) {
        int maxDimension = MAX_DIMENSION;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public SafeSearchTask getSafeSearchTask(Bitmap bitmap) {
        return new SafeSearchTask(bitmap);
    }

    public class SafeSearchTask extends AsyncTask<String, Void, Boolean> {
        HttpURLConnection connection;
        // WeakReference<Bitmap> mBitmapRef;
        Bitmap mBitmap;
        int type = 0;

        public SafeSearchTask(Bitmap bitmap) {
            // mBitmapRef = new WeakReference(bitmap);

            if (bitmap != null) {
                mBitmap = bitmap.copy(bitmap.getConfig(), true);

            } else {
                mBitmap = null;

            }
        }

        protected void onPreExecute() {
            super.onPreExecute();

            // Bitmap bitmap = mBitmapRef.get();
            Bitmap bitmap = mBitmap;
            if (bitmap != null)
                setCacheResult(bitmap, CacheResult.PROCESSING);
        }

        @Override
        protected Boolean doInBackground(String... arg0) {

            // Bitmap bitmap = mBitmapRef.get();
            Bitmap bitmap = mBitmap;

            int cnt = PinkAPICounter.addAndGet(1);

            Log.i("PinkCounter", "API: " + String.valueOf(cnt));

            if (bitmap == null)
                return true;

            Boolean filter = true;
            try {
                if (DEBUG) {
                    connection = getConnection_debug();

                } else {
                    connection = getConnection();

                }
                JSONObject request = generateBody(bitmap);
                OutputStream os = connection.getOutputStream();
                os.write(request.toString().getBytes());
                os.flush();

                String response;
                int responseCode = connection.getResponseCode();
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;

                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();

                response = new String(byteData);
                JSONObject responseJSON = new JSONObject(response);

                JSONObject safesearchannotation = responseJSON.getJSONArray("responses").getJSONObject(0)
                        .getJSONObject("safeSearchAnnotation");

                String adult = safesearchannotation.getString("adult");
                String medical = safesearchannotation.getString("medical");
                String violence = safesearchannotation.getString("violence");

                if (SAFESEARCH_LEVEL.get(adult) >= FILTER_LEVEL_INT || SAFESEARCH_LEVEL.get(medical) >= FILTER_LEVEL_INT
                        || SAFESEARCH_LEVEL.get(violence) >= FILTER_LEVEL_INT) {
                } else {
                    filter = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (filter) {
                setCacheResult(bitmap, CacheResult.UNSAFE);

            } else {
                setCacheResult(bitmap, CacheResult.SAFE);
            }

            return filter;
        }

        protected void onPostExecute(Boolean filter) {

            Log.i("PinkVision", "Calling invalidateAll!");
            pinkViewInvalidator.invalidateAll();

        }

    }

}
