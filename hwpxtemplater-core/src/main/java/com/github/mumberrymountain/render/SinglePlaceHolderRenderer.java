package com.github.mumberrymountain.render;

import com.github.mumberrymountain.linkedobj.LinkedRunItem;
import com.github.mumberrymountain.render.placeholder.PlaceHolder;

public interface SinglePlaceHolderRenderer {
    public void renderReplacement(LinkedRunItem linkedRunItem, PlaceHolder placeHolder, Object value);
}
