/**
 * FeedEx
 *
 * Copyright (c) 2012-2013 Frederic Julian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cgnet.swara.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cgnet.swara.MainApplication;
import org.cgnet.swara.R;
import org.cgnet.swara.provider.FeedData;
import org.cgnet.swara.provider.FeedData.EntryColumns;
import org.cgnet.swara.utils.UiUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends BaseAdapter {

    private static final int POS_ID = 0;
    private static final int POS_URL = 1;
    private static final int POS_NAME = 2;
    private static final int POS_IS_GROUP = 3;
    private static final int POS_GROUP_ID = 4;
    private static final int POS_ICON = 5;
    private static final int POS_LAST_UPDATE = 6;
    private static final int POS_ERROR = 7;
    private static final int POS_UNREAD = 8;

    private static final int ITEM_PADDING = UiUtils.dpToPixel(20);
    private static final int NORMAL_TEXT_COLOR = Color.parseColor("#EEEEEE");
    private static final int GROUP_TEXT_COLOR = Color.parseColor("#BBBBBB");

    private static final String COLON = MainApplication.getContext().getString(R.string.colon);

    private static final int CACHE_MAX_ENTRIES = 100;
    private final Map<Long, String> mFormattedDateCache = new LinkedHashMap<Long, String>(CACHE_MAX_ENTRIES + 1, .75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
            return size() > CACHE_MAX_ENTRIES;
        }
    };

    private final Context mContext;
    private Cursor mFeedsCursor;
    private int mAllUnreadNumber, mFavoritesNumber;

    private static class ViewHolder {
        public ImageView iconView;
        public TextView titleTxt;
        public TextView stateTxt;
        public TextView unreadTxt;
        public View separator;
    }

    public DrawerAdapter(Context context, Cursor feedCursor) {
        mContext = context;
        mFeedsCursor = feedCursor;

        updateNumbers();
    }

    public void setCursor(Cursor feedCursor) {
        mFeedsCursor = feedCursor;

        updateNumbers();
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_drawer_list, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            holder.titleTxt = (TextView) convertView.findViewById(android.R.id.text1);
            holder.stateTxt = (TextView) convertView.findViewById(android.R.id.text2);
            holder.unreadTxt = (TextView) convertView.findViewById(R.id.unread_count);
            holder.separator = convertView.findViewById(R.id.separator);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        // default init
        holder.iconView.setImageDrawable(null);
        holder.titleTxt.setText("");
        holder.titleTxt.setTextColor(NORMAL_TEXT_COLOR);
        holder.titleTxt.setAllCaps(false);
        holder.stateTxt.setVisibility(View.GONE);
        holder.unreadTxt.setText("");
        convertView.setPadding(0, 0, 0, 0);
        holder.separator.setVisibility(View.GONE);

        if (position == 0 || position == 1) {
            holder.titleTxt.setText(position == 0 ? R.string.all : R.string.favorites);
            holder.iconView.setImageResource(position == 0 ? R.drawable.ic_statusbar_rss : R.drawable.dimmed_rating_important);

            int unread = position == 0 ? mAllUnreadNumber : mFavoritesNumber;
            if (unread != 0) {
                holder.unreadTxt.setText(String.valueOf(unread));
            }
        } else if (position == 3) {
            holder.titleTxt.setText(R.string.search_entries);
            holder.iconView.setImageResource(R.drawable.action_search);
        }  else if (position == 2){
            holder.titleTxt.setText(R.string.bultoo_entries);
            holder.iconView.setImageResource(R.drawable.ic_bluetooth);
        }/*else if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 3)) {
        	
            holder.titleTxt.setText((mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME)));

            if (mFeedsCursor.getInt(POS_IS_GROUP) == 1) {
                holder.titleTxt.setTextColor(GROUP_TEXT_COLOR);
                holder.titleTxt.setAllCaps(true);
                holder.separator.setVisibility(View.INVISIBLE);
            } else {
                holder.stateTxt.setVisibility(View.INVISIBLE);

                if (mFeedsCursor.isNull(POS_ERROR)) {
                    long timestamp = mFeedsCursor.getLong(POS_LAST_UPDATE);

                    // Date formatting is expensive, look at the cache
                    String formattedDate = mFormattedDateCache.get(timestamp);
                    if (formattedDate == null) {

                        formattedDate = mContext.getString(R.string.update) + COLON;

                        if (timestamp == 0) {
                            formattedDate += mContext.getString(R.string.never);
                        } else {
                            formattedDate += StringUtils.getDateTimeString(timestamp);
                        }

                        mFormattedDateCache.put(timestamp, formattedDate);
                    }

//                    holder.stateTxt.setText(formattedDate);
                      holder.stateTxt.setText("");
                    
                } else {
                	  holder.stateTxt.setText("");
  //                  holder.stateTxt.setText(new StringBuilder(mContext.getString(R.string.error)).append(COLON).append(mFeedsCursor.getString(POS_ERROR)));
                }

                final long feedId = mFeedsCursor.getLong(POS_ID);
                Bitmap bitmap = UiUtils.getFaviconBitmap(feedId, mFeedsCursor, POS_ICON);

                if (bitmap != null) {
                    holder.iconView.setImageBitmap(bitmap);
                } else {
                    holder.iconView.setImageResource(R.drawable.icon);
                }

                int unread = mFeedsCursor.getInt(POS_UNREAD);
                if (unread != 0) {
                    holder.unreadTxt.setText(String.valueOf(unread));
                }
            }

            if (!mFeedsCursor.isNull(POS_GROUP_ID)) { // First level
                convertView.setPadding(ITEM_PADDING, 0, 0, 0);
            }
        } */

        return convertView;
    }

    @Override
    public int getCount() {
        if (mFeedsCursor != null) {
            return mFeedsCursor.getCount() + 3;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 3)) {
            return mFeedsCursor.getLong(POS_ID);
        }

        return -1;
    }

    public byte[] getItemIcon(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 3)) {
            return mFeedsCursor.getBlob(POS_ICON);
        }

        return null;
    }

    public String getItemName(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 3)) {
            return mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME);
        }

        return null;
    }

    public boolean isItemAGroup(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 3)) {
            return mFeedsCursor.getInt(POS_IS_GROUP) == 1;
        }

        return false;
    }

    private void updateNumbers() {
        mAllUnreadNumber = mFavoritesNumber = 0;

        // Gets the numbers of entries (should be in a thread, but it's way easier like this and it shouldn't be so slow)
        Cursor numbers = mContext.getContentResolver().query(EntryColumns.CONTENT_URI, new String[]{FeedData.ALL_UNREAD_NUMBER, FeedData.FAVORITES_NUMBER}, null, null, null);
        if (numbers != null) {
            if (numbers.moveToFirst()) {
                mAllUnreadNumber = numbers.getInt(0);
                mFavoritesNumber = numbers.getInt(1);
            }
            numbers.close();
        }
    }
}
