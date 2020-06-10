An Android app to stabilize text on the screen in case of shaking. So far I've developed a basic naive physics implementation--it just uses the linear accelerometer data to calculate the displacement of the phone and adjusts the text in the opposite direction, with adjustable friction factors. I've also implemented a version from Lin Zhong's NoShake paper, which models a spring-dampener system on the screen on the x and y axes. You can switch between the two versions by uncommenting the appropriate function call within the onSensorChanged function. Adjustable constants for the naive implementation are in Constants.java, and constants for the spring implementation are at the top of MainActivity. The spring implementation uses a circular buffer C++ API.
