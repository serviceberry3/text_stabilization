# A Content Stabilization Solution for Smartphones #

An Android app to stabilize text or other screen content in case of shaking. Three implementations to choose from:
* Naive physics implementation--it uses the linear accelerometer data to integrate displacement of the phone and adjusts the text in the opposite direction, with adjustable friction factors.
* Lin Zhong's NoShake paper, which models a spring-dampener system on the screen on the x and y axes. 
* Another system model using the recurrent least-squares estimation algorithm, based on paper by Wei, Hsiao, Jiang.    

You can switch between the versions by changing the ```mImplType``` variable at the top of MainActivity. Adjustable constants for each implementation are in *Constants.java. The two system model implementations use a circular buffer C++ API and some other C++ APIs via the Android NDK. Rendering is done using OpenGL ES code.  

Compare to [this](https://github.com/serviceberry3/noshake_lowest) test version in C, which interacts directly with the Direct Rendering Manager to draw on the screen.  
