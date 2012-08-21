package com.mot.ajaxwebapp;

import android.app.ListFragment;

/**
 * things I learned on writing testable clean code:
 * 1. programming to the interface
 * 2. always put your own wrapper around framework lib and delegate.
 * You can delegate, intercept, mock everything inside the wrapper.
 * 
 * Two ways, you can do extends the base class, or you can do wrapper with delegation.
 * 1. If relationship is inheritance, do extends.
 * 2. If relationship is composite, i.e. constructor is called, then use wrapper and delegation.
 */

/**
 * abstract class as a wrapper, the same effect as Interface.
 * this is the reverse of delegation method call back to base class implement.
 * this defines necessary IF API methods in base, and sub class implemnts thems.
 * Hence base class can be used as the highest layer during abstraction.
 */
public abstract class FragmentWrapper extends ListFragment {
    
    public abstract boolean goBack();  // subclass implement the actual method.
    
}
