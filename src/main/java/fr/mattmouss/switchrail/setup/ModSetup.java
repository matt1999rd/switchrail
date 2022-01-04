package fr.mattmouss.switchrail.setup;

import fr.mattmouss.switchrail.network.Networking;

public class ModSetup {
    public void init(){
        Networking.registerMessages();
    }
}
