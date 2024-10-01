package fr.moonshade.switchrail.gui;

import fr.moonshade.switchrail.other.Vector2i;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.regex.Pattern;

// this text field is special and only allow digit number to be written with limit lowLimit and highLimit and a specific responder
public class NumberTextField  extends EditBox {

    public NumberTextField(Font font, int initialValue, Vector2i relative, int offsetX, int offsetY, int lowLimit, int highLimit, Consumer<String> responder){
        super(font, relative.x+offsetX, relative.y+offsetY, 38,14, Component.nullToEmpty("Number Text Field"));
        this.setValue(String.valueOf(initialValue));
        String regex = "(-)?[0-9]+";
        this.setFilter(s -> {
            if (!Pattern.matches(regex,s.subSequence(0,s.length()))){
                return false;
            }
            int value = Integer.parseInt(s);
            return value>= lowLimit && value<= highLimit;
        });
        this.setResponder(responder);
    }

    public void add(int value){
        int oldValue = Integer.parseInt(getValue());
        this.setValue(String.valueOf(oldValue + value));
    }
}
