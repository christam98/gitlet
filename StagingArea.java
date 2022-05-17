package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.*;
import java.io.File;

public class StagingArea implements Serializable {

    public HashMap<String, String> addFiles;
    public ArrayList<String> rmFiles;
    public String currentBranch;

    public StagingArea() { // when the StagingArea is initialized
        this.addFiles = new HashMap<>();
        this.rmFiles = new ArrayList();
        this.currentBranch = "master";
    }

    public static void saveFiles(StagingArea files){
        Utils.writeContents(Repository.INDEX, Utils.serialize(files));
    }

    public static StagingArea fromFiles() {
        StagingArea result = Utils.readObject(Repository.INDEX, StagingArea.class);
        return result;
    }
}
