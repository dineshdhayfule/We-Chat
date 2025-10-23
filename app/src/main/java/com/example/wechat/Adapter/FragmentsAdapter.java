package com.example.wechat.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.wechat.Fragments.ChatsFragment;

public class FragmentsAdapter extends FragmentStatePagerAdapter {
    private ChatsFragment chatsFragment;

    public FragmentsAdapter(@NonNull FragmentManager fm) {
        super(fm);
        chatsFragment = new ChatsFragment();
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
       switch (position)
       {
           case 0 : return chatsFragment;
           default: return chatsFragment;
       }
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        String title = null;
        if (position==0)
        {
            title ="";
        }


        return title;
    }

    public ChatsFragment getChatsFragment() {
        return chatsFragment;
    }
}
