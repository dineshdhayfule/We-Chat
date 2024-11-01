// Generated by view binder compiler. Do not edit!
package com.example.wechat.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.wechat.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySignUpBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnGoogle;

  @NonNull
  public final Button btnSignUp;

  @NonNull
  public final ImageView imageView;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView txtAlreadyHaveAccount;

  @NonNull
  public final EditText txtEmail;

  @NonNull
  public final EditText txtPassword;

  @NonNull
  public final EditText txtUsername;

  private ActivitySignUpBinding(@NonNull LinearLayout rootView, @NonNull Button btnGoogle,
      @NonNull Button btnSignUp, @NonNull ImageView imageView, @NonNull TextView textView,
      @NonNull TextView txtAlreadyHaveAccount, @NonNull EditText txtEmail,
      @NonNull EditText txtPassword, @NonNull EditText txtUsername) {
    this.rootView = rootView;
    this.btnGoogle = btnGoogle;
    this.btnSignUp = btnSignUp;
    this.imageView = imageView;
    this.textView = textView;
    this.txtAlreadyHaveAccount = txtAlreadyHaveAccount;
    this.txtEmail = txtEmail;
    this.txtPassword = txtPassword;
    this.txtUsername = txtUsername;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySignUpBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySignUpBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_sign_up, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySignUpBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnGoogle;
      Button btnGoogle = ViewBindings.findChildViewById(rootView, id);
      if (btnGoogle == null) {
        break missingId;
      }

      id = R.id.btnSignUp;
      Button btnSignUp = ViewBindings.findChildViewById(rootView, id);
      if (btnSignUp == null) {
        break missingId;
      }

      id = R.id.imageView;
      ImageView imageView = ViewBindings.findChildViewById(rootView, id);
      if (imageView == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.txtAlreadyHaveAccount;
      TextView txtAlreadyHaveAccount = ViewBindings.findChildViewById(rootView, id);
      if (txtAlreadyHaveAccount == null) {
        break missingId;
      }

      id = R.id.txtEmail;
      EditText txtEmail = ViewBindings.findChildViewById(rootView, id);
      if (txtEmail == null) {
        break missingId;
      }

      id = R.id.txtPassword;
      EditText txtPassword = ViewBindings.findChildViewById(rootView, id);
      if (txtPassword == null) {
        break missingId;
      }

      id = R.id.txtUsername;
      EditText txtUsername = ViewBindings.findChildViewById(rootView, id);
      if (txtUsername == null) {
        break missingId;
      }

      return new ActivitySignUpBinding((LinearLayout) rootView, btnGoogle, btnSignUp, imageView,
          textView, txtAlreadyHaveAccount, txtEmail, txtPassword, txtUsername);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
