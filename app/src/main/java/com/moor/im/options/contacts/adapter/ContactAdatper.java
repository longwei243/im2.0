package com.moor.im.options.contacts.adapter;

import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.common.views.recyclerviewsidebar.EasyRecyclerSectionIndexer;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasyImageSection;
import com.moor.im.common.views.recyclerviewsidebar.sections.EasySection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/4/18.
 */
public class ContactAdatper  extends EasyRecyclerViewAdapter
        implements EasyRecyclerSectionIndexer<EasySection> {

    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private List<EasySection> easySections;

    /**
     * Please return RecyclerView loading layout Id array
     * 请返回RecyclerView加载的布局Id数组
     *
     * @return 布局Id数组
     */
    @Override public int[] getItemLayouts() {
        return new int[] { R.layout.item_image_section };
    }


    /**
     * butt joint the onBindViewHolder and
     * If you want to write logic in onBindViewHolder, you can write here
     * 对接了onBindViewHolder
     * onBindViewHolder里的逻辑写在这
     *
     * @param viewHolder viewHolder
     * @param position position
     */
    @Override public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        Contacts contacts = this.getItem(position);
        if (contacts == null) return;
        TextView headerTv = viewHolder.findViewById(R.id.section_header_tv);
        ImageView sectionIv = viewHolder.findViewById(R.id.section_iv);
        TextView nameTv = viewHolder.findViewById(R.id.section_name_tv);

        if (!TextUtils.isEmpty(contacts.displayName)) {
            nameTv.setText(contacts.displayName);
        } else {
            nameTv.setText("");
        }
        if(contacts.top && contacts.resId != 0) {
            //上面
            GlideUtils.displayNative(sectionIv, contacts.resId);
        }else {
            if (contacts.im_icon != null && !"".equals(contacts.im_icon)) {
                GlideUtils.displayNet(sectionIv, contacts.im_icon + M7Constant.QINIU_IMG_ICON);
            } else {
                GlideUtils.displayNative(sectionIv, R.drawable.img_default_head);
            }
        }
        this.setHeaderLogic(contacts, headerTv, viewHolder, position);

    }


    /**
     * Set header logic
     *
     * @param contacts contacts
     * @param headerTv headerTv
     * @param viewHolder viewHolder
     * @param position position
     */
    public void setHeaderLogic(Contacts contacts, TextView headerTv, EasyRecyclerViewHolder viewHolder, int position) {
        if (position != 0 && !contacts.top) {
            Contacts pre = this.getItem(position - 1);
            if (pre.top || !contacts.header.equals(pre.header)) {
                this.setHeader(true, headerTv, contacts.header);
            } else {
                this.setHeader(false, headerTv, null);
            }
        } else {
            this.setHeader(false, headerTv, null);
        }
    }


    /**
     * 如果是多布局的话，请写判断逻辑
     * 单布局可以不写
     *
     * @param position Item position
     * @return 布局Id数组中的index
     */
    @Override public int getRecycleViewItemType(int position) {
        return 0;
    }


    public void setHeader(boolean visible, TextView headerTv, String header) {
        if (visible) {
            headerTv.setText(header);
            headerTv.setVisibility(View.VISIBLE);
        } else {
            headerTv.setVisibility(View.GONE);
        }
    }


    @Override public List<EasySection> getSections() {
        this.resetSectionCache();

        int itemCount = getItemCount();
        if (itemCount < 1) return this.easySections;

        String letter;

        for (int i = 0; i < itemCount; i++) {
            Contacts contacts = this.getItem(i);
            letter = contacts.header;
            int section = this.easySections.size() == 0 ? 0 : this.easySections.size() - 1;
            if (contacts.top) {
                if (i != 0) section++;
                this.positionOfSection.put(section, i);
                this.easySections.add(
                        new EasyImageSection(contacts.resId, this.getEasyImageSection(), i));
            } else {
                // A B C D E F ...
                if (section < this.easySections.size()) {
                    EasySection easySection = this.easySections.get(section);
                    if (easySection instanceof EasyImageSection) {
                        // last section = image section
                        this.easySections.add(new EasySection(letter));
                        section++;
                        this.positionOfSection.put(section, i);
                    } else {
                        // last section = letter section
                        if (!this.easySections.get(section).letter.equals(letter)) {
                            this.easySections.add(new EasySection(letter));
                            section++;
                            this.positionOfSection.put(section, i);
                        }
                    }
                }else if (section == 0) {
                    this.easySections.add(new EasySection(letter));
                    this.positionOfSection.put(section, i);
                }
            }
            this.sectionOfPosition.put(i, section);
        }
        return this.easySections;
    }


    private void resetSectionCache() {
        if (this.easySections == null) this.easySections = new ArrayList<>();
        if (this.easySections.size() > 0) this.easySections.clear();
        if (sectionOfPosition == null) this.sectionOfPosition = new SparseIntArray();
        if (this.sectionOfPosition.size() > 0) this.sectionOfPosition.clear();
        if (this.positionOfSection == null) this.positionOfSection = new SparseIntArray();
        if (this.positionOfSection.size() > 0) this.positionOfSection.clear();
    }


    public @EasyImageSection.ImageType int getEasyImageSection() {
        return EasyImageSection.CIRCLE;
    }


    @Override public int getPositionForSection(int sectionIndex) {
        return positionOfSection.get(sectionIndex);
    }


    @Override public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }
}
