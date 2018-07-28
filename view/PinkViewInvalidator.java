package android.view;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

public class PinkViewInvalidator {

    private static PinkViewInvalidator instance = new PinkViewInvalidator();

    private static LinkedList<WeakReference<View>> mViewList = new LinkedList<WeakReference<View>>();

    public static PinkViewInvalidator getInstance() {

        return instance;
    }

    private PinkViewInvalidator() {
        Log.i("PinkViewInvalidator", "Starting");
    }

    public synchronized void addView(View view) {
        mViewList.add(new WeakReference(view));
    }

    public synchronized void invalidateAll() {
        for (Iterator<WeakReference<View>> iterator = mViewList.iterator(); iterator.hasNext();) {
            WeakReference<View> weakRef = iterator.next();
            View ref = weakRef.get();
            if (ref != null) {
                // Strongly referenced
                // Log.d("PinkViewInvalidator", ref.toString());
                ref.destroyDrawingCache();
                ref.postInvalidate();

            } else {
                // Freed
                iterator.remove();

            }
        }
    }

}