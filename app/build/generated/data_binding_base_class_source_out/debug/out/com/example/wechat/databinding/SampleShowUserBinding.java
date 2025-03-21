// Generated by view binder compiler. Do not edit!
package com.example.wechat.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.wechat.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class SampleShowUserBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView lastMessage;

  @NonNull
  public final CircleImageView profileImage;

  @NonNull
  public final TextView userNameList;

  private SampleShowUserBinding(@NonNull LinearLayout rootView, @NonNull TextView lastMessage,
      @NonNull CircleImageView profileImage, @NonNull TextView userNameList) {
    this.rootView = rootView;
    this.lastMessage = lastMessage;
    this.profileImage = profileImage;
    this.userNameList = userNameList;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static SampleShowUserBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static SampleShowUserBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.sample_show_user, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static SampleShowUserBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.lastMessage;
      TextView lastMessage = ViewBindings.findChildViewById(rootView, id);
      if (lastMessage == null) {
        break missingId;
      }

      id = R.id.profile_image;
      CircleImageView profileImage = ViewBindings.findChildViewById(rootView, id);
      if (profileImage == null) {
        break missingId;
      }

      id = R.id.userNameList;
      TextView userNameList = ViewBindings.findChildViewById(rootView, id);
      if (userNameList == null) {
        break missingId;
      }

      return new SampleShowUserBinding((LinearLayout) rootView, lastMessage, profileImage,
          userNameList);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
