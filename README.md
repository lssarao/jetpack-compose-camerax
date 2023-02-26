## CameraX - Camera App

Android application that uses the CameraX library to implement the camera feature. The app allows users to take photos using their phone's camera and save to external storage. As well as sharing features, users can easily share captured images with other apps.

## Demo

<img src="https://user-images.githubusercontent.com/65452331/221429538-c45d13c7-5c7c-4e72-b96e-47777e288ece.jpg" width="23%"></img> <img src="https://user-images.githubusercontent.com/65452331/221429540-fd2debed-4207-44b2-9c45-b516a9190f82.jpg" width="23%"></img> <img src="https://user-images.githubusercontent.com/65452331/221429543-c1271905-f547-4ca3-a7b1-fc2246eda6d8.jpg" width="23%"></img> <img src="https://user-images.githubusercontent.com/65452331/221429546-2c89e0c5-20bb-46ea-8b79-99091bea193f.jpg" width="23%"></img> 

Click [here](https://appetize.io/app/6stafh5pjqv43vccftgzpzf234) to try out our sample app without downloading or installing anything.

## Tech Stack

**Programming Language:** Kotlin

**UI:** Jetpack Compose

**Development Tools:** Android Studio IDE

**Libraries and Frameworks:**

 - CameraX library to implement the camera feature
 - Implemented a camera preview through AndroidView
 - Handles runtime read and write permission requests for camera access and external storage access
 - Sharing captured image to other apps with send Intent
 
 - Coil for preview of the captured images

 - Kotlin Coroutines for managing background tasks

 - Gradle for building and dependency management

**Version Control:** Git

**Testing:** 

 - JUnit and Mockito for unit testing
 - Espresso for UI testing

## Roadmap

- [x] Sharing feature
- [ ] Camera Modes
- [ ] Add more integrations

## Installation

To build the app from this repository, follow these steps:

- Clone the repository to your local machine using HTTP: git clone https://github.com/lssarao/jetpack-compose-camerax.git

- Open Android Studio on your local machine and click on open an existing Android Studio project.

- Browse to the directory where you cloned the repository and import it.

- Once the project is imported, you can build this app by clicking the run button in Android Studio.

- Select your device or an emulator from the available options and and install the app.

If you encounter any issues or errors during the setup or building process, feel free to raise an issue on the repository.


## Contribute

I will be more than happy to receive your PR, I am open to suggestions or modifications.

## Feedback
If you have any feedback, please reach out to us at lssarao411@gmail.com

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/lssarao/jetpack-compose-camerax/blob/master/LICENSE) file for details.
