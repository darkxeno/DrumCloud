package com.codefixia.drumcloud;

import java.io.File;

public class FileItem {
    private final String name;
    private final FileType type;
    private final File file;
    public String downloadUrl;
    public String fileId;

    public FileItem(String name, FileType type, File file) {
      this.name = name;
      this.type = type;
      this.file = file;
    }

	public String getName() {
      return name;
    }

    public FileType getType() {
      return type;
    }

    public File getFile() {
      return file;
    }

    public String getFullPath() {
      return file.getAbsolutePath();
    }

    @Override
    public String toString() {
      return getName();
    }
  }