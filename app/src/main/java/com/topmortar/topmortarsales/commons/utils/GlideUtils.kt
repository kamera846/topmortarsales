package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.topmortar.topmortarsales.R

object GlideUtil {

    fun loadImage(context: Context, uri: Uri?, imageView: ImageView) {
        if (uri == null) {
            imageView.setImageResource(R.drawable.logo_light_horizontal)
            return
        }

        Glide.with(context)
            .load(uri)
            .fitCenter()
            .placeholder(R.drawable.bg_light_dark_round)
            .error(R.drawable.logo_light_horizontal)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}
