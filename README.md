# Android Simple Autofill Demo

A minimal Android autofill service implementation for reproducing and testing browser autofill
issues.

## Requirements

- **Android Studio**: Narwhal
- **AGP**: 8.10.0
- **Kotlin**: 2.1.0
- **Java**: 17
- **Min SDK**: 30
- **Target SDK**: 36

## Purpose

This project serves as a minimal reproduction case for testing autofill behavior across different
browsers and applications. It implements a basic autofill service that fills form fields with test
data to help identify and debug autofill-related issues.

## Key Components

- **MyAutoFillService.kt** - Main autofill service implementation
- **auto_fill_service.xml** - Service configuration and metadata
- Simple UI for enabling/configuring the autofill service

## Setup

1. Clone the repository
2. Open in Android Studio Narwhal
3. Build and install on device/emulator
4. Go to Settings > System > Languages & input > Autofill service
5. Select "Simple Autofill Demo" as your autofill service
