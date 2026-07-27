# Link Encoder


## Functionality

Adds a message intercept that takes sent links and encodes them with Base64, alongside taking recieved B64 encoded text, 
checking if they are encoded links and then decoding them before handing the plaintext back to the user. 
The links that are handed to the user are highlighted and underlined to identify them, as well as being clickable.
Additionally, when a url belongs to an image, the user can hover their cursor over it to preview the linked image.
###### Note: Links can only be shared with other mod users, someone without the mod cannot see your links and without it, you cannot see the links of someone who uses it.

## Reasoning / History

On the Minecraft server Hypixel, there is a game they named Skyblock, this used to run on version 1.8.9 and the server 
allowed for urls to be sent. This allowed for people to send image links, and there were mods that gave functionality
to view these images with only a hover of the cursor. Now, you cannot send links and when you try you are greeted with 
the message "Advertising is against the rules. You will receive a punishment on the server if you attempt to advertise."
So now that images not being sent and the version has been updated to modern, the mods that let you view these images 
don't exist anymore. \
\
This mod was made with the purpose of bringing these images back, specifically the Dungeons "270 Score" and "300 Score"
messages that came from user's mods, which were holding their own custom images. My friends and I always found these
images quite humorous, and we always looked forward to seeing new images, although were quite disappointed after 
learning of their removal. So I personally decided to seek a solution that helps people who are interested in 
having it back.

## How to Use?

In it's most basic form, you just send a url and the mod will do everything for you! Any link the mod recognises as
an image, you can hover your cursor over it to view the image in the selected location. \
The mod is configured using commands:
```
/linkencoder toggle encode|decode|preview        # toggling of each feature, all enabled by default
/linkencoder colour <rgb>                        # link colour, default is 4f89d9
/linkencoder position [top|bottom]_[left|right]  # image preview position, default is bottom_right
/linkencoder offset <x> <y>                      # offset from selected corner
/linkencoder size <horizontal> <vertical>        # image preview size, expands away from and shrinks into corner
```

## Disclaimer / Warning! - Use on servers at your own risk

I (developer) do not take any responsibility for and possible punishments relating to the use of this mod.

###### Note: I believe it to be a fun and harmless mod, specifically for the entertainment of only the people who use it, but use it at your own risk.
