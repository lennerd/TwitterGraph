package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.ExpiringEntity;

import java.io.*;

public final class SkyCache extends Thread {

    private final File file;

    public SkyCache(File file) {
        this.file = file;
    }

    public Sky restore(int field, int amount) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            File parent = new File(this.file.getParent());

            if (!parent.exists()) {
                parent.mkdirs();
            }

            this.file.createNewFile();

            return null;
        }

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            try {
                Object obj = objectInputStream.readObject();
                Sky sky = new Sky(field, amount);

                while (obj != null) {
                    if (!(obj instanceof ExpiringEntity)) {
                        throw new InvalidClassException("Expected expiring EntityContainer, got \"" + obj.getClass() + "\"");
                    }

                    sky.addEntityContainer((ExpiringEntity) obj);
                    obj = objectInputStream.readObject();
                }

                for (SkyObject drawable : sky.getDrawables()) {
                    drawable.reset();
                }

                return sky;
            } finally {
                objectInputStream.close();
            }
        } catch (EOFException e) {
            return null;
        }
    }

    public void cache(Sky sky) throws IOException {
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(this.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            File parent = new File(this.file.getParent());

            if (!parent.exists()) {
                parent.mkdirs();
            }

            this.file.createNewFile();
            this.cache(sky);

            return;
        }

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        try {
            for (ExpiringEntity entityContainer : sky.getEntityContainers()) {
                objectOutputStream.writeObject(entityContainer);
            }
            objectOutputStream.writeObject(null);
        } finally {
            objectOutputStream.close();
        }
    }

}
