# Chat SDK Configuration

Chat SDK offers a wide range of configuration options. These options can be specified during the initialization phase:

```
ChatSDK.builder().configure()

        // Configure Chat SDK here

        .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
        .setAnonymousLoginEnabled(false)
        .setDebugModeEnabled(true)
        .setRemoteConfigEnabled(true)
        .setIdenticonType(Config.IdenticonType.Gravatar)
        .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
        .setDisablePresence(false)
        .setSendSystemMessageWhenRoleChanges(true)
        .build()

        // Add modules
        
        .addModule(
        
                // You can also set configuration options for the module
                // If available
                
                FirebaseModule.configure()
                        .setFirebaseRootPath(rootPath)
                        .setDisableClientProfileUpdate(false)
                        .setEnableCompatibilityWithV4(true)
                        .setDevelopmentModeEnabled(true)
                        .build()
        )

        // Activate
        
        .build()
        .activate(this);
```

- We start the Chat SDK configuration builder
- Configure as necessary. You can find a full list of configuration options [here]
- Add the modules
	- You must add one module that provides a network adapter. This could be:
		- FirebaseModule
		- FireStreamModule
		- XMPPModule
	- You must add one module that provides an interface adapter. The default is:
		- DefaultUIModule	 	
- Modules can be configured by calling `module.configure()`
- In Android Studio you can easily see a list of config options. As you type `.configure...` you will see an auto-complete box pop up. Inside that box it will say "see also". Click this and it will open the configuration class which has a list of available methods
 
## Configure Object 
 
There is another option to set the configuration which is to use a `Configure` object. Here is an example:

```
FirestreamModule.configure(config -> {
    config.setRoot("");
});
```

Here you implement a lamda block which is provided with the instance of the `config` class. 

There is no difference between using the builder pattern or the configure pattern. It's just down to personal preference. 

One benefit of the configure pattern is that you can add logic into the configuration for example:

```
FirestreamModule.configure(config -> {
	if(env.equals("prod")) {
    	config.setRoot("live");
	} else {
	    config.setRoot("demo");
	}
});
```

That can make it easy to have multiple different configurations for different environments. 
	