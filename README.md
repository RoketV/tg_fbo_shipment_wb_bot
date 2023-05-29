# Telegram Bot for Wildberries Marketplace
This Telegram bot is designed to assist sellers on the Wildberries online marketplace by simplifying the process of creating and formatting excel files for shipment arrangements. The bot offers various features to streamline the workflow and enhance the user experience. https://t.me/Wb_Fbo_Shipment_Bot

## Project Structure
The project consists of four modules:

- Dispatcher: This module receives requests from users and handles the communication with the bot.
- Node: The main service module that contains the core business logic. It interacts with the dispatcher module using RabbitMQ for seamless communication.
- JPA: This module is responsible for managing the database operations related to the bot's functionality.
- Util: Contains utility classes and constants that are shared across the application.
## Business Logic
The bot's primary purpose is to help sellers on the Wildberries marketplace. It facilitates the creation of excel files required for the shipment of products to Wildberries' warehouse. The process involves two types of files: initial files with barcodes and data files.

**The key features of the bot include:**

- File Handling: Sellers can upload excel files or send text messages containing relevant information. The bot automatically processes and populates the appropriate fields in the Wildberries file, saving valuable time for the user.
- Prevalidation: The bot performs prevalidation on all files to ensure they meet the required formatting and data standards.
- Sample Requests: Sellers can request samples of both the initial barcode document and the data document. The bot responds by sending the respective samples.
**Libraries Used**
The project utilizes the Apache POI library for working with excel files. This library provides powerful tools and utilities for reading, writing, and manipulating excel documents, enabling seamless excel file handling within the bot.

## Getting Started
To use the Telegram bot for Wildberries Marketplace, follow these steps:

1. Install Telegram on your device.
2. Search for the bot using the specified name or username. https://t.me/Wb_Fbo_Shipment_Bot
3. Start a conversation with the bot and begin interacting using the available commands and features.
## Usage
The bot offers the following commands and functionalities:

1. Upload an excel file with barcodes or with data. The bot will find out which file you have sent, so you do not need to worry about it. The bot populates the initial barcode document with data, if you have sent one before.
/sample_with_skue: Provides the seller with a sample of the initial barcode document.
/sample_with_data: Sends a sample of the data document to the seller.
/help: Provides with information on bot's functionality
/start: Start!
Please note that the bot performs prevalidation on the uploaded files and messages to ensure their compatibility and accuracy. Bot uses PostgreSQL database.

### Contributions
Contributions to the project are welcome. If you find any issues, have suggestions for improvements, or would like to contribute new features, please submit an issue or a pull request on the project's GitHub repository.

### License
This project is licensed under the MIT License. Feel free to use, modify, and distribute the code for personal or commercial purposes.
