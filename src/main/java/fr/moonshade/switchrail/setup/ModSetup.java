package fr.moonshade.switchrail.setup;

import fr.moonshade.switchrail.network.Networking;

public class ModSetup {
    public void init(){
        Networking.registerMessages();
    }
}
