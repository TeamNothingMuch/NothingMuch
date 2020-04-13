package dev.jahir.blueprint.ui.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.jahir.blueprint.R
import dev.jahir.blueprint.data.listeners.HomeItemsListener
import dev.jahir.blueprint.data.models.Counter
import dev.jahir.blueprint.data.models.HomeItem
import dev.jahir.blueprint.data.models.Icon
import dev.jahir.blueprint.data.models.IconsCounter
import dev.jahir.blueprint.data.models.KustomCounter
import dev.jahir.blueprint.data.models.WallpapersCounter
import dev.jahir.blueprint.data.models.ZooperCounter
import dev.jahir.blueprint.extensions.animateVisibility
import dev.jahir.blueprint.extensions.defaultLauncher
import dev.jahir.blueprint.extensions.executeLauncherIntent
import dev.jahir.blueprint.ui.activities.BlueprintActivity
import dev.jahir.blueprint.ui.activities.BlueprintKuperActivity
import dev.jahir.blueprint.ui.adapters.HomeAdapter
import dev.jahir.blueprint.ui.decorations.HomeGridSpacingItemDecoration
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.dpToPx
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.setPaddingBottom
import dev.jahir.kuper.extensions.hasStoragePermission

@SuppressLint("MissingPermission")
class HomeFragment : Fragment(R.layout.fragment_home), HomeItemsListener {

    private val wallpaper: Drawable?
        get() = activity?.let {
            try {
                val wm = WallpaperManager.getInstance(it)
                if (it.hasStoragePermission) wm?.fastDrawable else null
            } catch (e: Exception) {
                null
            }
        }

    private val staticWallpaper: Drawable?
        get() = activity?.let {
            try {
                it.drawable(it.string(R.string.static_icons_preview_picture))
            } catch (e: Exception) {
                null
            }
        }

    private val rightWallpaper: Drawable?
        get() = activity?.let {
            if (it.boolean(R.bool.static_icons_preview_picture_by_default))
                staticWallpaper ?: wallpaper
            else wallpaper ?: staticWallpaper
        }

    private val adapter: HomeAdapter by lazy {
        HomeAdapter(context?.boolean(R.bool.show_overview, true) == true, this)
    }

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swipeRefreshLayout: SwipeRefreshLayout? by view.findView(R.id.swipe_to_refresh)
        swipeRefreshLayout?.isEnabled = false

        recyclerView = view.findViewById(R.id.recycler_view)
        val quickApplyBtn: AppCompatButton? by view.findView(R.id.quick_apply_btn)
        val showQuickApplyBtn = context?.defaultLauncher != null
        (activity as? BlueprintActivity)?.bottomNavigation?.let {
            it.post {
                view.setPaddingBottom(it.measuredHeight)
                recyclerView?.setPaddingBottom(
                    it.measuredHeight
                            + (if (showQuickApplyBtn) quickApplyBtn?.measuredHeight ?: 0 else 0)
                            + 4.dpToPx
                )
            }
        }

        val columnsCount = 2
        val layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = layoutManager
        adapter.setLayoutManager(layoutManager)

        adapter.wallpaper = rightWallpaper
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(HomeGridSpacingItemDecoration(columnsCount, 8.dpToPx))
        adapter.showOverview = context?.boolean(R.bool.show_overview, true) == true
        (activity as? BlueprintActivity)?.repostCounters()

        quickApplyBtn?.animateVisibility(showQuickApplyBtn)
        quickApplyBtn?.setOnClickListener { context?.executeLauncherIntent(context?.defaultLauncher) }
    }

    internal fun updateIconsPreview(icons: List<Icon>) {
        adapter.iconsPreviewList = ArrayList(icons)
    }

    internal fun updateHomeItems(items: List<HomeItem>) {
        adapter.homeItems = ArrayList(items)
    }

    internal fun updateWallpaper() {
        adapter.wallpaper = rightWallpaper
    }

    internal fun updateIconsCount(count: Int) {
        adapter.iconsCount = count
    }

    internal fun updateWallpapersCount(count: Int) {
        adapter.wallpapersCount = count
    }

    internal fun updateKustomCount(count: Int) {
        adapter.kustomCount = count
    }

    internal fun updateZooperCount(count: Int) {
        adapter.zooperCount = count
    }

    override fun onIconsPreviewClicked() {
        super.onIconsPreviewClicked()
        (activity as? BlueprintActivity)?.loadPreviewIcons(true)
    }

    override fun onCounterClicked(counter: Counter) {
        super.onCounterClicked(counter)
        when (counter) {
            is IconsCounter -> {
                (activity as? BlueprintActivity)?.bottomNavigation
                    ?.setSelectedItemId(R.id.icons, true)
            }
            is WallpapersCounter -> {
                (activity as? BlueprintActivity)?.bottomNavigation
                    ?.setSelectedItemId(R.id.wallpapers, true)
            }
            is KustomCounter, is ZooperCounter -> {
                (activity as? BlueprintActivity)?.let {
                    it.startActivity(Intent(it, BlueprintKuperActivity::class.java))
                }
            }
        }
    }

    override fun onAppLinkClicked(url: String, intent: Intent?) {
        super.onAppLinkClicked(url, intent)
        intent?.let { activity?.startActivity(it) } ?: { context?.openLink(url) }()
    }

    companion object {
        internal const val TAG = "home_fragment"
    }
}