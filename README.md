# Attention !
This is a copy & paste from a private repo that was worked on during a university course.
I got the permission of everyone involved to upload it here as a public repo so people can easily access the code.
# Battery Monitoring App

This app was developed as part of the Software Engineering Labs module at Ruhr University Bochum. It works with data from a battery in a RUB Motorsports vehicle, reading the data stream sent by the battery controller via a WebSocket and displaying it appropriately.

## SDK Version and Libraries
- **SDK Version:** This project uses Android 14.0 and should work on newer devices. It may be possible to run it on slightly older versions, though this has not been tested.
- **Libraries:** All used libraries can be found in the `libs.versions.toml` file.

## Setup
Follow the steps below to get the app running in your environment.
### 1.  Installation
Clone this repository and open it in Android Studio
### 2. Connecting with the WebSocket

Before you begin, check whether you are using HTTP or HTTPS, and whether basic authentication is required. The setup differs slightly depending on your needs.

All necessary changes should be made in the `WebSocketClient` class, within the `connect()` method. The methods required are already included in the `connect()` method, so you will likely only need to uncomment them (be sure to comment/remove the `run` method you do not need).
- Connecting with basic authentication
    - Use the method `this.run(String username, String password, String url, boolean https)` and replace the placeholders with your credentials. Set `https` to `true` if you are using a secure connection.
- Connecting without Basic Authentication
    - Use the method `this.run(String url, boolean https)` and replace `url` with your domain/IP address, setting `https` accordingly.

You can adjust the rate that messages are decoded by editing the `decodeRate` variable in the same class. 

**Important: HTTP Setup**
To use HTTP, you need to add the following line to the application tag in your `AndroidManifest.xml`:
`android:usesCleartextTraffic="true"`

### 3. Sending Emergency Emails
To enable notifications via email, update the credentials for the sending email account in the `AlertSender` class. Change the following fields:
- `this.host` to the hostname of the outgoing SMTP server.
- `this.port` to the corresponding port number.
- `this.email` to the email address from which the notifications will be sent.
- `this.password` to the password for the above email address.

**Note:** The current values in these fields are placeholders provided for testing and grading purposes by the SE Labs team. These credentials will be deleted after the project is graded, so it is recommended to replace them with your own credentials.

### 4. Sending SMS

SMS notifications are sent automatically when the corresponding checkbox is enabled in the Settings Menu. Be aware that costs may be incurred for sending SMS.

## How to use this App
To learn how to use this app, please watch our demo video located in the `demo` folder.

## Tablet View
The layout works on tablets just as well as on phones.

## License

This app is published under the MIT License.
