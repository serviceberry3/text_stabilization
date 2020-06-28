package weiner.noah.openglbufftesting;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class OpenGLView extends GLSurfaceView {
    Context myContext;
    Activity myActivity;

    public OpenGLView(Context context) {
        super(context);
        myContext = context;
        //init();
    }

    private void init() {
        //set embedded OpenGL version
        setEGLContextClientVersion(2);

        setPreserveEGLContextOnPause(true);
        setRenderer(new OpenGLRenderer(myContext, myActivity));
    }
}
