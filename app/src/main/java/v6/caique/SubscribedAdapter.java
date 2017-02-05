package v6.caique;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class SubscribedAdapter extends ArrayAdapter<String> {

    private LayoutInflater vi;
    private Context context;

    public SubscribedAdapter(Context c)
    {
        super(c, R.layout.list_item_chat, CacheChats.Subs);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        Log.d("Position", position + " ");

        if (row == null) {
            row = vi.inflate(R.layout.list_item_chat, null);
        }

        if (CacheChats.Loaded.size() > position)
        {
            final String ChatId = CacheChats.Subs.get(position);
            final CacheChats.ChatStructure Chat = CacheChats.Loaded.get(ChatId);

            final ImageView imageView = (ImageView) row.findViewById(R.id.chatdp);
            imageView.setImageDrawable(null);

            final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("chats/" + ChatId);
            storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    Glide.with(context)
                            .using(new FirebaseImageLoader())
                            .load(storageRef)
                            .centerCrop()
                            .bitmapTransform(new CropCircleTransformation(context))
                            .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                            .into(imageView);
                }
            });

            TextView nameTextView = (TextView) row.findViewById(R.id.itemname);
            nameTextView.setText(Chat.Title);

            if (Chat.Messages.size() != 0)
            {
                CacheChats.MessageStructure Msg = Chat.Messages.getLast();
                TextView descTextView = (TextView) row.findViewById(R.id.itemdesc);
                descTextView.setText(CacheChats.Name(Msg.Sender, "Unknown") + ": " + Msg.Content);
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newChatActivity = new Intent(MainActivity.Instance, ChatActivity.class);
                    Bundle b = new Bundle();
                    b.putString("chat", ChatId);
                    newChatActivity.putExtras(b);
                    context.startActivity(newChatActivity);
                }
            });
        }

        return row;
    }
}