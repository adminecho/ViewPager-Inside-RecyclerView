package com.viewpagerinsiderecyclerview;


import android.os.Build;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.atomic.AtomicBoolean;

import android.transition.TransitionSet;



import static com.viewpagerinsiderecyclerview.ImageData.IMAGE_DRAWABLES;



public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ImageViewHolder> {

    private interface ViewHolderListener {

        void onLoadCompleted(ImageView view, int adapterPosition);

        void onItemClicked(View view, int adapterPosition);
    }

    private final RequestManager requestManager;
    private final ViewHolderListener viewHolderListener;


    public GridAdapter(Fragment fragment) {
        this.requestManager = Glide.with(fragment);
        this.viewHolderListener = new ViewHolderListenerImpl(fragment);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        return new ImageViewHolder(view, requestManager, viewHolderListener);
    }


    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.onBind();
    }
    @Override
    public int getItemCount() {
        return IMAGE_DRAWABLES.length;
    }



    private static class ViewHolderListenerImpl implements ViewHolderListener {

        private Fragment fragment;
        private AtomicBoolean enterTransitionStarted;

        ViewHolderListenerImpl(Fragment fragment) {
            this.fragment = fragment;
            this.enterTransitionStarted = new AtomicBoolean();
        }

        @Override
        public void onLoadCompleted(ImageView view, int position) {

            if (MainActivity.currentPosition != position) {
                return;
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return;
            }
            fragment.startPostponedEnterTransition();
        }


        @Override
        public void onItemClicked(View view, int position) {
            MainActivity.currentPosition = position;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((TransitionSet) fragment.getExitTransition()).excludeTarget(view, true);
            }

            ImageView transitioningView = view.findViewById(R.id.card_image);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.getFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true) // Optimize for shared element transition
                        .addSharedElement(transitioningView, transitioningView.getTransitionName())
                        .replace(R.id.fragment_container, new ImagePagerFragment(), ImagePagerFragment.class
                                .getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        }
    }


    static class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final ImageView image;
        private final RequestManager requestManager;
        private final ViewHolderListener viewHolderListener;

        ImageViewHolder(View itemView, RequestManager requestManager,
                        ViewHolderListener viewHolderListener) {
            super(itemView);
            this.image = itemView.findViewById(R.id.card_image);
            this.requestManager = requestManager;
            this.viewHolderListener = viewHolderListener;
            itemView.findViewById(R.id.card_view).setOnClickListener(this);
        }


        void onBind() {
            int adapterPosition = getAdapterPosition();
            setImage(adapterPosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.setTransitionName(String.valueOf(IMAGE_DRAWABLES[adapterPosition]));
            }
        }

        void setImage(final int adapterPosition) {
            requestManager
                    .load(IMAGE_DRAWABLES[adapterPosition])
                    .listener(new RequestListener<Integer, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                            viewHolderListener.onLoadCompleted(image, adapterPosition);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            viewHolderListener.onLoadCompleted(image, adapterPosition);
                            return false;
                        }
                    }).into(image);
        }

        @Override
        public void onClick(View view) {

            viewHolderListener.onItemClicked(view, getAdapterPosition());
        }
    }
}
