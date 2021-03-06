package com.lufficc.ishuhui.data.source.file.remote;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.lufficc.ishuhui.data.source.file.FilesDataSource;
import com.lufficc.ishuhui.data.source.file.local.FilesLocalDataSource;
import com.lufficc.ishuhui.model.FileEntry;
import com.lufficc.ishuhui.utils.AppUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by lufficc on 2016/11/5.
 */

public class FilesRemoteDataSource implements FilesDataSource {
    private static FilesRemoteDataSource INSTANCE;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executorService = AppUtils.getExecutorService();

    private FilesRemoteDataSource() {
    }

    public static FilesRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (FilesLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FilesRemoteDataSource();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void refresh(String chapterId) {

    }

    @Override
    public List<FileEntry> getFiles(String chapterId) {
        List<FileEntry> fileEntries = null;
        try {
            fileEntries = new ArrayList<>();
            Document document = Jsoup.connect(AppUtils.getChapterUrl(chapterId)).get();
            Elements images = document.getElementsByTag("img");
            int i = 1;
            for (Element img : images) {
                String src = img.attr("src");
                FileEntry fileEntry = new FileEntry();
                fileEntry.setTitle(String.valueOf(i));
                fileEntry.setUrl(src);
                fileEntry.setChapterId(chapterId);
                fileEntries.add(fileEntry);
                i++;
            }
            return fileEntries;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileEntries;
    }

    @Override
    public void getFiles(String chapterId, @NonNull LoadFilesCallback callback) {
        executorService.execute(new GetChapterImagesTask(chapterId, callback));
    }

    @Override
    public void getFile(@NonNull String fileId, @NonNull GetFileCallback callback) {

    }

    @Override
    public long saveFile(FileEntry file) {
        return 0;
    }

    @Override
    public void saveFiles(List<FileEntry> files) {

    }

    private class GetChapterImagesTask implements Runnable {
        private String chapterId;
        private LoadFilesCallback callback;

        GetChapterImagesTask(String chapterId, LoadFilesCallback callback) {
            this.chapterId = chapterId;
            this.callback = callback;
        }

        @Override
        public void run() {
            final List<FileEntry> fileEntries = getFiles(chapterId);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (fileEntries == null) {
                        callback.onLoadedFailed();
                    } else {
                        callback.onFileLoaded(fileEntries);
                    }
                }
            });
        }
    }
}
