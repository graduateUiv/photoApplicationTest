package com.example.testtestaddphoto;

public class Folder {
    private String folderName;

    public Folder(String name) {
        setFolderName(name);
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String newFolderName) {
        this.folderName = newFolderName;
    }
}
