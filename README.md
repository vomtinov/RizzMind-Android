# RizzMind-Android 🚀

## Phase 2: GPT-Powered Reply Suggestion Engine

An intelligent Android app that provides **contextual, emotionally-aware reply suggestions** using AI. The app features a floating bubble overlay that analyzes chat conversations and generates smart response suggestions powered by OpenAI's GPT-4.

## 🌟 Features

### Core Functionality
- **Floating Bubble Interface**: Draggable overlay bubble that works across all apps
- **GPT-4 Integration**: Contextual reply suggestions using OpenAI's latest model
- **Local Fallback**: Pattern-based suggestions when offline or without API key
- **Conversation Memory**: Stores last 200 messages per person for context
- **Smart Suggestions**: 1-5 customizable suggestions with confidence scoring

### Technical Highlights
- **Modern Architecture**: Clean Architecture with MVVM pattern
- **Material 3 Design**: Beautiful, responsive UI with Jetpack Compose
- **Room Database**: Efficient local storage for conversation history
- **Retrofit Integration**: Robust API communication with error handling
- **Overlay Service**: System-level floating bubble with proper permissions

## 🏗️ Architecture

### Module Structure
```
app/src/main/java/com/rizzmind/android/
├── data/                    # Room database entities and DAOs
├── memory/                  # Conversation memory management
├── service/                 # Floating bubble service
├── suggestor/              # AI-powered suggestion engine
├── ui/theme/               # Compose UI theming
├── utils/                  # Utility classes
└── MainActivity.kt         # Configuration screen
```

### Key Components

#### 1. SuggestionEngine (`suggestor/`)
- **GPT-4 Integration**: Contextual suggestions via OpenAI API
- **Local Patterns**: Fallback suggestions based on text analysis
- **Smart Parsing**: Extracts and formats conversation context
- **Error Handling**: Graceful degradation for network issues

#### 2. MemoryManager (`memory/`)
- **Conversation Storage**: Automatic chat history tracking
- **Context Formatting**: Prepares data for AI consumption
- **Message Trimming**: Keeps last 200 messages per person
- **Activity Detection**: Identifies recent conversation activity

#### 3. FloatingBubbleService (`service/`)
- **System Overlay**: Floating bubble across all apps
- **Drag & Drop**: Moveable bubble with touch handling
- **Bottom Sheet UI**: Beautiful suggestion display
- **Clipboard Integration**: One-tap copy functionality

#### 4. Database Layer (`data/`)
- **Room Database**: Efficient SQLite abstraction
- **ChatMessage Entity**: Timestamped conversation storage
- **Automatic Cleanup**: Maintains optimal storage size

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0)
- OpenAI API key (optional, for GPT features)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/RizzMind-Android.git
   cd RizzMind-Android
   ```

2. Open in Android Studio and sync project

3. Run the app on device or emulator

### Configuration
1. **Start the App**: Launch RizzMind from your app drawer
2. **Set API Key**: Enter your OpenAI API key in settings (optional)
3. **Grant Permissions**: Allow overlay permission for floating bubble
4. **Start Service**: Tap "Start Floating Bubble"
5. **Test Suggestions**: Tap the floating bubble to see suggestions

## ⚙️ Configuration Options

### AI Settings
- **Suggestion Mode**: Choose between GPT-4 or local patterns
- **API Key**: Configure OpenAI API key for GPT features
- **Suggestion Count**: Adjust number of suggestions (1-5)

### Service Controls
- **Start/Stop Service**: Control floating bubble visibility
- **Overlay Permissions**: Automatic permission handling

## 🔧 Technical Details

### Dependencies
- **Jetpack Compose**: Modern UI toolkit
- **Room Database**: Local data persistence
- **Retrofit**: HTTP client for API calls
- **Coroutines**: Asynchronous programming
- **Material 3**: Latest Material Design components

### API Integration
```kotlin
// Example GPT-4 request format
{
  "model": "gpt-4",
  "messages": [
    {
      "role": "system",
      "content": "Generate witty, charming text replies..."
    },
    {
      "role": "user", 
      "content": "Chat history: [Girl 8:15 PM]: What's up?"
    }
  ],
  "max_tokens": 200,
  "temperature": 0.8
}
```

### Database Schema
```sql
CREATE TABLE chat_memory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    sender TEXT NOT NULL,
    message TEXT NOT NULL,
    personIdentifier TEXT NOT NULL
);
```

## 🔒 Privacy & Security

- **Local Storage**: All conversations stored locally on device
- **API Key Security**: Keys stored in encrypted SharedPreferences
- **No Data Collection**: No analytics or user data transmission
- **Permission Transparency**: Clear explanation of required permissions

## 🗺️ Roadmap

### Phase 3 (Planned)
- **OCR Integration**: Real-time text extraction from chat apps
- **PEFT Models**: Fine-tuned local AI models
- **Smart Contact Detection**: Automatic person identification
- **Conversation Analytics**: Response effectiveness tracking

### Future Enhancements
- **Multi-language Support**: International conversation contexts
- **Personality Modes**: Different suggestion styles (professional, casual, flirty)
- **Integration APIs**: Direct integration with popular messaging apps
- **Cloud Sync**: Optional conversation backup and sync

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- OpenAI for GPT-4 API
- Android team for Material 3 and Compose
- Contributors and testers

---

**Built with ❤️ for better conversations**
