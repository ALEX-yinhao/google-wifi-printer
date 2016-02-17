package com.example.wonders.mobile.printer;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    public static final String LOG_TAG = "PrintActivity";
    private static final int PAGE_COUNT = 5;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testactivity);
        button = (Button)findViewById(R.id.print);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_print) {
            printView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void printView() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        final View view = findViewById(R.id.content);
        printManager.print("Print_View",
                new PrintDocumentAdapter() {
                    private static final int RESULT_LAYOUT_FAILED = 1;
                    private static final int RESULT_LAYOUT_FINISHED = 2;
                    private PrintAttributes mPrintAttributes;

                    @Override
                    public void onLayout(final PrintAttributes oldAttributes,
                                         final PrintAttributes newAttributes,
                                         final CancellationSignal cancellationSignal,
                                         final LayoutResultCallback callback,
                                         final Bundle metadata) {
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected void onPreExecute() {
                                // First register for cancellation requests.
                                cancellationSignal.setOnCancelListener(new OnCancelListener() {
                                    @Override
                                    public void onCancel() {
                                        cancel(true);
                                    }
                                });
                                mPrintAttributes = newAttributes;
                            }

                            @Override
                            protected Integer doInBackground(Void... params) {
                                try {
                                    // Pretend we do some layout work.
                                    for (int i = 0; i < PAGE_COUNT; i++) {
                                        // Be nice and respond to cancellation.
                                        if (isCancelled()) {
                                            return null;
                                        }
                                        pretendDoingLayoutWork();
                                    }
                                    return RESULT_LAYOUT_FINISHED;
                                } catch (Exception e) {
                                    return RESULT_LAYOUT_FAILED;
                                }
                            }

                            @Override
                            protected void onPostExecute(Integer result) {
                                // The task was not cancelled, so handle the layout result.
                                switch (result) {
                                    case RESULT_LAYOUT_FINISHED: {
                                        PrintDocumentInfo info = new PrintDocumentInfo
                                                .Builder("print_view.pdf")
                                                .setContentType(PrintDocumentInfo
                                                        .CONTENT_TYPE_DOCUMENT)
                                                .setPageCount(PAGE_COUNT)
                                                .build();
                                        callback.onLayoutFinished(info, false);
                                    }
                                    break;
                                    case RESULT_LAYOUT_FAILED: {
                                        callback.onLayoutFailed(null);
                                    }
                                    break;
                                }
                            }

                            @Override
                            protected void onCancelled(Integer result) {
                                // Task was cancelled, report that.
                                callback.onLayoutCancelled();
                            }

                            private void pretendDoingLayoutWork() throws Exception {
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }

                    @Override
                    public void onWrite(final PageRange[] pages,
                                        final ParcelFileDescriptor destination,
                                        final CancellationSignal canclleationSignal,
                                        final WriteResultCallback callback) {
                        new AsyncTask<Void, Void, Integer>() {
                            private static final int RESULT_WRITE_FAILED = 1;
                            private static final int RESULT_WRITE_FINISHED = 2;
                            private final SparseIntArray mWrittenPages = new SparseIntArray();
                            private final PrintedPdfDocument mPdfDocument = new PrintedPdfDocument(
                                    TestActivity.this, mPrintAttributes);

                            @Override
                            protected void onPreExecute() {
                                // First register for cancellation requests.
                                canclleationSignal.setOnCancelListener(new OnCancelListener() {
                                    @Override
                                    public void onCancel() {
                                        cancel(true);
                                    }
                                });
                                for (int i = 0; i < PAGE_COUNT; i++) {
                                    // Be nice and respond to cancellation.
                                    if (isCancelled()) {
                                        return;
                                    }
                                    // Write the page only if it was requested.
                                    if (containsPage(pages, i)) {
                                        mWrittenPages.append(mWrittenPages.size(), i);
                                        PdfDocument.Page page = mPdfDocument.startPage(i);
                                        // The page of the PDF backed canvas size is in pixels (1/72") and
                                        // smaller that the view. We scale down the drawn content and to
                                        // fit. This does not lead to losing data as PDF is a vector format.
                                        final float scale = (float) Math.min(mPdfDocument.getPageWidth(),
                                                mPdfDocument.getPageHeight()) / Math.max(view.getWidth(), view.getHeight());
                                        page.getCanvas().scale(scale, scale);
                                        view.draw(page.getCanvas());
                                        mPdfDocument.finishPage(page);
                                    }
                                }
                            }

                            @Override
                            protected Integer doInBackground(Void... params) {
                                // Write the data and return success or failure.
                                try {
                                    mPdfDocument.writeTo(new FileOutputStream(
                                            destination.getFileDescriptor()));
                                    return RESULT_WRITE_FINISHED;
                                } catch (IOException ioe) {
                                    return RESULT_WRITE_FAILED;
                                }
                            }

                            @Override
                            protected void onPostExecute(Integer result) {
                                // The task was not cancelled, so handle the write result.
                                switch (result) {
                                    case RESULT_WRITE_FINISHED: {
                                        PageRange[] pageRanges = computePageRanges(mWrittenPages);
                                        callback.onWriteFinished(pageRanges);
                                    }
                                    break;
                                    case RESULT_WRITE_FAILED: {
                                        callback.onWriteFailed(null);
                                    }
                                    break;
                                }
                                mPdfDocument.close();
                            }

                            @Override
                            protected void onCancelled(Integer result) {
                                // Task was cancelled, report that.
                                callback.onWriteCancelled();
                                mPdfDocument.close();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }

                    private PageRange[] computePageRanges(SparseIntArray writtenPages) {
                        List<PageRange> pageRanges = new ArrayList<PageRange>();

                        int start = -1;
                        int end = -1;
                        final int writtenPageCount = writtenPages.size();
                        for (int i = 0; i < writtenPageCount; i++) {
                            if (start < 0) {
                                start = writtenPages.valueAt(i);
                            }
                            int oldEnd = end = start;
                            while (i < writtenPageCount && (end - oldEnd) <= 1) {
                                oldEnd = end;
                                end = writtenPages.valueAt(i);
                                i++;
                            }
                            PageRange pageRange = new PageRange(start, end);
                            pageRanges.add(pageRange);
                            start = end = -1;
                        }

                        PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
                        pageRanges.toArray(pageRangesArray);
                        return pageRangesArray;
                    }

                    private boolean containsPage(PageRange[] pageRanges, int page) {
                        final int pageRangeCount = pageRanges.length;
                        for (int i = 0; i < pageRangeCount; i++) {
                            if (pageRanges[i].getStart() <= page
                                    && pageRanges[i].getEnd() >= page) {
                                return true;
                            }
                        }
                        return false;
                    }
                }, null);
    }

}
