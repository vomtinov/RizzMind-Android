# RizzMind

An Android app that uses MediaProjection API to capture screen content and MLKit OCR to extract text from the screen. Features a floating bubble overlay that triggers OCR when tapped.

## Features

- 🎯 **Floating Bubble Overlay**: Always-on-top bubble that can be moved around the screen
- 📱 **Screen Capture**: Uses MediaProjection API to capture screen content
- 🔍 **OCR Text Extraction**: Powered by Google MLKit for accurate text recognition
- 📐 **MVVM Architecture**: Clean architecture with proper separation of concerns
- 🔐 **Permission Management**: Handles overlay and screen capture permissions gracefully
- 🌟 **Modern UI**: Material Design 3 with clean, intuitive interface

## Technical Specifications

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Key Libraries**:
  - Google MLKit Text Recognition
  - AndroidX Lifecycle components
  - View Binding & Data Binding
  - Kotlin Coroutines
  - Dexter for permission handling

## Permissions Required

- `SYSTEM_ALERT_WINDOW` - For displaying the floating bubble overlay
- `FOREGROUND_SERVICE` - For running the bubble service in the background
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` - For media projection foreground service
- `POST_NOTIFICATIONS` - For service notifications

## Project Structure

```
app/
├── src/main/java/com/rizzmind/app/
│   ├── data/
│   │   └── repository/
│   │       └── OCRRepository.kt           # Handles MLKit OCR operations
│   ├── service/
│   │   └── FloatingBubbleService.kt       # Foreground service for floating bubble
│   ├── ui/
│   │   ├── MainActivity.kt                # Main activity with permission handling
│   │   └── viewmodel/
│   │       ├── MainViewModel.kt           # ViewModel for MVVM pattern
│   │       └── MainViewModelFactory.kt    # Factory for dependency injection
│   └── utils/
│       ├── PermissionHelper.kt            # Permission management utilities
│       └── ScreenCaptureManager.kt        # MediaProjection and screen capture
└── src/main/res/
    ├── layout/
    │   ├── activity_main.xml              # Main activity layout
    │   └── floating_bubble.xml            # Floating bubble layout
    ├── values/
    │   ├── strings.xml                    # String resources
    │   ├── colors.xml                     # Color definitions
    │   └── themes.xml                     # App themes
    └── xml/
        ├── backup_rules.xml               # Backup configuration
        └── data_extraction_rules.xml      # Data extraction rules
```

## How It Works

1. **Permission Request**: App requests overlay permission and screen capture permission
2. **Service Start**: FloatingBubbleService starts as a foreground service
3. **Bubble Display**: A floating bubble is displayed over other apps
4. **Screen Capture**: When bubble is tapped, MediaProjection captures the screen
5. **OCR Processing**: MLKit processes the captured image to extract text
6. **Result Display**: Extracted text is shown in a Toast message

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RizzMind
   ```

2. **Set up Android SDK**
   - Update `local.properties` with your Android SDK path:
     ```
     sdk.dir=/path/to/android/sdk
     ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## Usage

1. **Launch the app** and grant required permissions
2. **Tap "Start Floating Bubble"** to begin the service
3. **The blue OCR bubble** will appear on your screen
4. **Navigate to any app** with text you want to extract
5. **Tap the floating bubble** to capture and process the screen
6. **View extracted text** in the Toast message

## Key Components

### FloatingBubbleService
- Manages the floating overlay window
- Handles screen capture via MediaProjection
- Processes OCR using MLKit
- Displays results via Toast

### MainActivity
- Handles permission requests
- Manages service lifecycle
- Implements MVVM pattern with data binding

### OCRRepository
- Encapsulates MLKit text recognition logic
- Provides clean API for bitmap-to-text conversion
- Handles ML model lifecycle

### PermissionHelper
- Simplifies overlay permission management
- Handles runtime permissions with Dexter
- Provides user-friendly permission dialogs

## Dependencies

```kotlin
// Core Android libraries
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")

// Architecture components
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// ML Kit OCR
implementation("com.google.mlkit:text-recognition:16.0.0")

// Permissions
implementation("com.karumi:dexter:6.2.3")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Known Limitations

- OCR accuracy depends on text clarity and size
- Screen capture may not work on some protected content
- Floating bubble requires overlay permission which may be restricted on some devices
- Performance may vary based on device capabilities

## Future Enhancements

- [ ] Support for multiple languages in OCR
- [ ] Text history and clipboard integration
- [ ] Customizable bubble appearance
- [ ] Batch text processing
- [ ] Text translation features

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Troubleshooting

### Common Issues

**Overlay permission denied**: 
- Go to Settings > Apps > Special access > Display over other apps
- Find RizzMind and enable the permission

**Screen capture not working**:
- Ensure you granted the screen capture permission
- Restart the app if issues persist

**OCR not detecting text**:
- Ensure text is clear and well-lit
- Try capturing a larger area of text
- Check if the content allows screen capture

**Service stops unexpectedly**:
- Check battery optimization settings
- Ensure the app has permission to run in background
