package com.example.chengen.mupetune;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chengen on 2016-04-01.
 */
public interface Communicator {
    public void respond(int position, ArrayList<File>songs);
}
