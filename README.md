# Link Encoder


## Functionality

Adds a message intercept that takes sent links and encodes them with Base64, alongside taking recieved B64 encoded text, checking if they are encoded links and then decoding them before handing the plaintext back to the user.
The links that are handed to the user are highlighted and underlined to identify them, as well as being clickable.
Additionally, when a url belongs to an image, the user can hover their cursor over it to preview the linked image.

