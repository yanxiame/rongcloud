package io.rong.imkit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.assist.FailReason;
import io.rong.imageloader.core.assist.ImageScaleType;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.model.Event;
import io.rong.imkit.plugin.image.HackyViewPager;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.utils.ImageDownloadManager;
import io.rong.imlib.RongCommonDefine;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.subscaleview.ImageSource;
import io.rong.subscaleview.SubsamplingScaleImageView;

public class PictureActivity extends FragmentActivity implements View.OnLongClickListener{
    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LangUtils.getConfigurationContext(newBase);
        super.attachBaseContext(context);
    }

    private static final String TAG = "PictureActivity";
    private static final int IMAGE_MESSAGE_COUNT = 10; //每次获取的图片消息数量。
    private HackyViewPager mViewPager;
    private ImageMessage mCurrentImageMessage;
    private Message mMessage;
    private Conversation.ConversationType mConversationType;
    private int mCurrentMessageId;
    private String mTargetId = null;
    private int mCurrentIndex = 0;
    private PictureActivity.ImageAdapter mImageAdapter;
    private boolean isFirstTime = false;

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            RLog.i(TAG, "onPageSelected. position:" + position);
            mCurrentIndex = position;
            View view = mViewPager.findViewById(position);
            if (view != null)
                mImageAdapter.updatePhotoView(position, view);
            if (position == (mImageAdapter.getCount() - 1)) {
                getConversationImageUris(mImageAdapter.getItem(position).getMessageId().getMessageId(), RongCommonDefine.GetMessageDirection.BEHIND);
            } else if (position == 0) {
                getConversationImageUris(mImageAdapter.getItem(position).getMessageId().getMessageId(), RongCommonDefine.GetMessageDirection.FRONT);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_fr_photo);
        Message currentMessage = getIntent().getParcelableExtra("message");
        mMessage = currentMessage;
        mCurrentImageMessage = (ImageMessage) currentMessage.getContent();
        mConversationType = currentMessage.getConversationType();
        mCurrentMessageId = currentMessage.getMessageId();
        mTargetId = currentMessage.getTargetId();

        mViewPager = (HackyViewPager) findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mImageAdapter = new PictureActivity.ImageAdapter();
        isFirstTime = true;
        if (!mMessage.getContent().isDestruct()) {
            getConversationImageUris(mCurrentMessageId, RongCommonDefine.GetMessageDirection.FRONT);  //获取当前点开图片之前的图片消息。
            getConversationImageUris(mCurrentMessageId, RongCommonDefine.GetMessageDirection.BEHIND);
        } else {
            //只显示1张照片
            ArrayList<ImageInfo> lists = new ArrayList<>();
            lists.add(new PictureActivity.ImageInfo(mMessage, mCurrentImageMessage.getThumUri(),
                    mCurrentImageMessage.getLocalUri() == null ? mCurrentImageMessage.getRemoteUri() : mCurrentImageMessage.getLocalUri()));
            mImageAdapter.addData(lists, true);
            mViewPager.setAdapter(mImageAdapter);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onEventMainThread(Event.RemoteMessageRecallEvent event) {
        if (mCurrentMessageId == event.getMessageId()) {
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage(getString(R.string.rc_recall_success))
                    .setPositiveButton(getString(R.string.rc_dialog_ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            mImageAdapter.removeRecallItem(event.getMessageId());
            mImageAdapter.notifyDataSetChanged();
            if (mImageAdapter.getCount() == 0) {
                finish();
            }
        }
    }

    public void onEventMainThread(Event.MessageDeleteEvent deleteEvent) {
        RLog.d(TAG, "MessageDeleteEvent");
        if (deleteEvent.getMessageIds() != null) {
            for (int messageId : deleteEvent.getMessageIds()) {
                mImageAdapter.removeRecallItem(messageId);
            }
            mImageAdapter.notifyDataSetChanged();
            if (mImageAdapter.getCount() == 0) {
                finish();
            }
        }
    }

    private void getConversationImageUris(int mesageId, final RongCommonDefine.GetMessageDirection direction) {
        if (mConversationType != null && !TextUtils.isEmpty(mTargetId)) {
            RongIMClient.getInstance().getHistoryMessages(mConversationType, mTargetId, "RC:ImgMsg", mesageId, IMAGE_MESSAGE_COUNT, direction, new RongIMClient.ResultCallback<List<Message>>() {
                @Override
                public void onSuccess(List<Message> messages) {
                    int i;
                    ArrayList<ImageInfo> lists = new ArrayList<>();
                    if (messages != null) {
                        if (direction.equals(RongCommonDefine.GetMessageDirection.FRONT))
                            Collections.reverse(messages);
                        for (i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getContent() instanceof ImageMessage && !message.getContent().isDestruct()) {
                                ImageMessage imageMessage = (ImageMessage) message.getContent();
                                Uri largeImageUri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();

                                if (imageMessage.getThumUri() != null && largeImageUri != null) {
                                    lists.add(new PictureActivity.ImageInfo(message, imageMessage.getThumUri(), largeImageUri));
                                }
                            }
                        }
                    }
                    if (direction.equals(RongCommonDefine.GetMessageDirection.FRONT) && isFirstTime) {
                        lists.add(new PictureActivity.ImageInfo(mMessage, mCurrentImageMessage.getThumUri(),
                                mCurrentImageMessage.getLocalUri() == null ? mCurrentImageMessage.getRemoteUri() : mCurrentImageMessage.getLocalUri()));
                        mImageAdapter.addData(lists, direction.equals(RongCommonDefine.GetMessageDirection.FRONT));
                        mViewPager.setAdapter(mImageAdapter);
                        isFirstTime = false;
                        mViewPager.setCurrentItem(lists.size() - 1);
                        mCurrentIndex = lists.size() - 1;
                    } else if (lists.size() > 0) {
                        mImageAdapter.addData(lists, direction.equals(RongCommonDefine.GetMessageDirection.FRONT));
                        mImageAdapter.notifyDataSetChanged();
                        if (direction.equals(RongCommonDefine.GetMessageDirection.FRONT)) {
                            mViewPager.setCurrentItem(lists.size());
                            mCurrentIndex = lists.size();
                        }
                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        //释放内存
        ImageLoader.getInstance().clearMemoryCache();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 图片长按处理事件
     *
     * @param v             查看图片的PhotoView
     * @param thumbUri      缩略图的Uri地址
     * @param largeImageUri 原图片的Uri地址
     * @return boolean 返回true不会执行默认的处理
     */
    public boolean onPictureLongClick(View v, Uri thumbUri, Uri largeImageUri) {
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
//        if (mCurrentImageMessage.isDestruct()) {
//            return false;
//        }
        PictureActivity.ImageInfo imageInfo = mImageAdapter.getImageInfo(mCurrentIndex);
        if (imageInfo != null) {
            Uri thumbUri = imageInfo.getThumbUri();
            Uri largeImageUri = imageInfo.getLargeImageUri();
            if (onPictureLongClick(v, thumbUri, largeImageUri))
                return true;
            final File file;
            if (largeImageUri != null) {
                if (largeImageUri.getScheme().startsWith("http") || largeImageUri.getScheme().startsWith("https"))
                    file = ImageLoader.getInstance().getDiskCache().get(largeImageUri.toString());
                else
                    file = new File(largeImageUri.getPath());
            } else {
                return false;
            }
            String[] items = new String[]{getString(R.string.rc_save_picture)};
            OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
                @Override
                public void onOptionsItemClicked(int which) {
                    if (which == 0) {
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (!PermissionCheckUtil.requestPermissions(PictureActivity.this, permissions)) {
                            return;
                        }

                        String saveImagePath = RongUtils.getImageSavePath(PictureActivity.this);
                        if (file != null && file.exists()) {
                            String name = System.currentTimeMillis() + ".jpg";
                            FileUtils.copyFile(file, saveImagePath + File.separator, name);
                            MediaScannerConnection.scanFile(PictureActivity.this, new String[]{saveImagePath + File.separator + name}, null, null);
                            Toast.makeText(PictureActivity.this, PictureActivity.this.getString(R.string.rc_save_picture_at), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PictureActivity.this, getString(R.string.rc_src_file_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).show();
        }
        return true;
    }

    private class ImageAdapter extends PagerAdapter {
        private ArrayList<ImageInfo> mImageList = new ArrayList<>();

        public class ViewHolder {
            ProgressBar progressBar;
            TextView progressText;
            SubsamplingScaleImageView photoView;
            TextView mCountDownView;
        }

        private View newView(final Context context, final PictureActivity.ImageInfo imageInfo) {
            View result = LayoutInflater.from(context).inflate(R.layout.rc_fr_image, null);

            PictureActivity.ImageAdapter.ViewHolder holder = new PictureActivity.ImageAdapter.ViewHolder();
            holder.progressBar = result.findViewById(R.id.rc_progress);
            holder.progressText = result.findViewById(R.id.rc_txt);
            holder.photoView = result.findViewById(R.id.rc_photoView);
            holder.mCountDownView = result.findViewById(R.id.rc_count_down);
            holder.photoView.setOnLongClickListener(PictureActivity.this);
            holder.photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Window window = PictureActivity.this.getWindow();
                    if (window != null) {
                        window.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    }
                    finish();
                }
            });

            result.setTag(holder);

            return result;
        }

        public void addData(ArrayList<ImageInfo> newImages, boolean direction) {
            if (newImages == null || newImages.size() == 0)
                return;
            if (mImageList.size() == 0) {
                mImageList.addAll(newImages);
            } else if (direction && !isFirstTime && !isDuplicate(newImages.get(0).getMessageId().getMessageId())) {
                ArrayList<ImageInfo> temp = new ArrayList<>();
                temp.addAll(mImageList);
                mImageList.clear();
                mImageList.addAll(newImages);
                mImageList.addAll(mImageList.size(), temp);
            } else if (!isFirstTime && !isDuplicate(newImages.get(0).getMessageId().getMessageId())) {
                mImageList.addAll(mImageList.size(), newImages);
            }
        }

        private boolean isDuplicate(int messageId) {
            for (PictureActivity.ImageInfo info : mImageList) {
                if (info.getMessageId().getMessageId() == messageId)
                    return true;
            }
            return false;
        }

        public PictureActivity.ImageInfo getItem(int index) {
            return mImageList.get(index);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            RLog.i(TAG, "instantiateItem.position:" + position);

            View imageView = newView(container.getContext(), mImageList.get(position));
            updatePhotoView(position, imageView);
            imageView.setId(position);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            RLog.i(TAG, "destroyItem.position:" + position);
            container.removeView((View) object);
        }

        private void removeRecallItem(int messageId) {
            for (int i = mImageList.size() - 1; i >= 0; i--) {
                if (mImageList.get(i).message.getMessageId() == messageId) {
                    mImageList.remove(i);
                    break;
                }
            }
        }

        private void updatePhotoView(final int position, View view) {
            File file;
            final PictureActivity.ImageAdapter.ViewHolder holder = (PictureActivity.ImageAdapter.ViewHolder) view.getTag();
            Uri originalUri = mImageList.get(position).getLargeImageUri();
            final Uri thumbUri = mImageList.get(position).getThumbUri();

            if (originalUri == null || thumbUri == null) {
                RLog.e(TAG, "large uri and thumbnail uri of the image should not be null.");
                return;
            }
            if (mCurrentImageMessage.isDestruct() && mMessage.getMessageDirection().equals(Message.MessageDirection.RECEIVE)) {
                DestructManager.getInstance().addListener(mMessage.getUId(), new PictureActivity.DestructListener(holder, mMessage.getUId()), TAG);
            }

            file = ImageLoader.getInstance().getDiskCache().get(originalUri.toString());

            if (file != null && file.exists()) {
                Uri resultUri = Uri.fromFile(file);
                String path = "";
                // 判断当前是否已加载了同样的uri，防止每次更新同样图片时图片闪动现象
                if (!resultUri.equals(holder.photoView.getUri())) {
                    if (resultUri.getScheme().equals("file")) {
                        path = resultUri.toString().substring(5);
                    } else if (resultUri.getScheme().equals("content")) {
                        Cursor cursor = getApplicationContext().getContentResolver().query(resultUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                        cursor.moveToFirst();
                        path = cursor.getString(0);
                        cursor.close();
                    }
                    RLog.i(TAG, "readPictureDegree = " + FileUtils.readPictureDegree(PictureActivity.this,path));
                    if (FileUtils.readPictureDegree(PictureActivity.this,path) == SubsamplingScaleImageView.ORIENTATION_90) {
                        holder.photoView.setOrientation(SubsamplingScaleImageView.ORIENTATION_90);
                    }
                    holder.photoView.setImage(ImageSource.uri(resultUri));
                }
                return;
            } else {
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.NONE)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .considerExifParams(true)
                        .build();

                ImageLoader.getInstance().loadImage(originalUri.toString(), null, options, new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.photoView.setImage(ImageSource.uri(thumbUri));
                                holder.progressText.setVisibility(View.VISIBLE);
                                holder.progressBar.setVisibility(View.VISIBLE);
                                holder.progressText.setText("0%");
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                if (imageUri.startsWith("file://")) {
                                    holder.progressText.setVisibility(View.GONE);
                                    holder.progressBar.setVisibility(View.GONE);
                                } else {
                                    ImageDownloadManager.getInstance().downloadImage(imageUri, new ImageDownloadManager.DownloadStatusListener() {
                                        @Override
                                        public void downloadSuccess(String localPath, Bitmap bitmap) {
                                            holder.photoView.setImage(ImageSource.uri(localPath));
                                            holder.progressText.setVisibility(View.GONE);
                                            holder.progressBar.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void downloadFailed(ImageDownloadManager.DownloadStatusError error) {
                                            holder.progressText.setVisibility(View.GONE);
                                            holder.progressBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                if (mCurrentImageMessage.isDestruct() && mMessage.getMessageDirection().equals(Message.MessageDirection.RECEIVE)) {
                                    DestructManager.getInstance().startDestruct(mMessage);
                                    EventBus.getDefault().post(new Event.changeDestructionReadTimeEvent(mMessage));
                                }
                                holder.progressText.setVisibility(View.GONE);
                                holder.progressBar.setVisibility(View.GONE);
                                File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
                                Uri resultUri = null;
                                if (file != null) {
                                    resultUri = Uri.fromFile(file);
                                }
                                holder.photoView.setBitmapAndFileUri(loadedImage, resultUri);

                                /*
                                 * 当同一个下标位置被同时刷新多次时，有时会只触发一次图片完成加载
                                 * 此时需要根据当前下标最新的view进行更新，防止旧的view消耗掉完成加载事件
                                 * 导致最新的view没有被更新加载后的图片
                                 */
                                View inPagerView = mViewPager.findViewById(position);
                                if (inPagerView != null) {
                                    final PictureActivity.ImageAdapter.ViewHolder inPagerHolder = (PictureActivity.ImageAdapter.ViewHolder) inPagerView.getTag();
                                    if (inPagerHolder != holder) {
                                        inPagerHolder.progressText.setVisibility(View.GONE);
                                        inPagerHolder.progressBar.setVisibility(View.GONE);
                                        mImageAdapter.updatePhotoView(position, inPagerView);
                                    }
                                }
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                                holder.progressText.setVisibility(View.GONE);
                                holder.progressText.setVisibility(View.GONE);
                            }
                        },
                        new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                holder.progressText.setText(current * 100 / total + "%");
                                if (current == total) {
                                    holder.progressText.setVisibility(View.GONE);
                                    holder.progressBar.setVisibility(View.GONE);
                                } else {
                                    holder.progressText.setVisibility(View.VISIBLE);
                                    holder.progressBar.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        }


        public PictureActivity.ImageInfo getImageInfo(int position) {
            return mImageList.get(position);
        }
    }

    private class ImageInfo {
        private Message message;
        private Uri thumbUri;
        private Uri largeImageUri;

        ImageInfo(Message message, Uri thumbnail, Uri largeImageUri) {
            this.message = message;
            this.thumbUri = thumbnail;
            this.largeImageUri = largeImageUri;
        }

        public Message getMessageId() {
            return message;
        }

        public Uri getLargeImageUri() {
            return largeImageUri;
        }

        public Uri getThumbUri() {
            return thumbUri;
        }
    }

    private static class DestructListener implements RongIMClient.DestructCountDownTimerListener {
        private WeakReference<ImageAdapter.ViewHolder> mHolder;
        private String mMessageId;

        public DestructListener(PictureActivity.ImageAdapter.ViewHolder pHolder, String pMessageId) {
            mHolder = new WeakReference<>(pHolder);
            mMessageId = pMessageId;
        }

        @Override
        public void onTick(long millisUntilFinished, String pMessageId) {
            if (mMessageId.equals(pMessageId)) {
                PictureActivity.ImageAdapter.ViewHolder viewHolder = mHolder.get();
                if (viewHolder != null) {
                    viewHolder.mCountDownView.setVisibility(View.VISIBLE);
                    viewHolder.mCountDownView.setText(String.valueOf(Math.max(millisUntilFinished, 1)));
                }
            }
        }


        @Override
        public void onStop(String messageId) {
            if (mMessageId.equals(messageId)) {
                PictureActivity.ImageAdapter.ViewHolder viewHolder = mHolder.get();
                if (viewHolder != null) {
                    viewHolder.mCountDownView.setVisibility(View.GONE);
                }
            }
        }
    }
}
