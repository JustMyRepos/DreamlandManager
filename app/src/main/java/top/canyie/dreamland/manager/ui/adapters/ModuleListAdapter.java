package top.canyie.dreamland.manager.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import top.canyie.dreamland.manager.R;
import top.canyie.dreamland.manager.core.Dreamland;
import top.canyie.dreamland.manager.core.ModuleInfo;
import top.canyie.dreamland.manager.ui.fragments.ModuleManagerFragment;
import top.canyie.dreamland.manager.utils.Intents;
import top.canyie.dreamland.manager.utils.ToastCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canyie
 */
public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ViewHolder> implements Filterable {
    private ModuleManagerFragment mFragment;
    private Context context;
    private List<ModuleInfo> mSourceList;
    private List<ModuleInfo> mFilteredList;
    private ModulesFilter mFilter;
    private OnModuleStateChangedListener mModuleStateChangedListener;

    public ModuleListAdapter(ModuleManagerFragment fragment) {
        this.mFragment = fragment;
        this.context = fragment.requireContext();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.moduleslist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleInfo module = mFilteredList.get(position);
        holder.name.setText(module.name);
        holder.description.setText(module.description);
        holder.version.setText(module.version);
        holder.icon.setImageDrawable(module.icon);
        holder.checkbox.setChecked(module.enabled);
        if (!Dreamland.isInstalled()) {
            holder.error.setText(R.string.framework_state_not_installed);
            holder.error.setVisibility(View.VISIBLE);
            holder.checkbox.setEnabled(false);
        } else if (!Dreamland.isActive()) {
            holder.error.setText(R.string.framework_state_installed_but_not_active);
            holder.error.setVisibility(View.VISIBLE);
            holder.checkbox.setEnabled(false);
        } else if (module.supported) {
            holder.checkbox.setOnCheckedChangeListener((view, isChecked) -> {
                getModuleInfoForPosition(position).setEnabled(isChecked);
                if (mModuleStateChangedListener != null)
                    mModuleStateChangedListener.onModuleStateChanged();
            });
        } else {
            holder.error.setText(R.string.module_error_not_support);
            holder.error.setVisibility(View.VISIBLE);
            holder.checkbox.setEnabled(false);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!Intents.openAppUserInterface(context, getModuleInfoForPosition(position).packageName)) {
                ToastCompat.showToast(context, R.string.alert_module_cannot_open);
            }
        });
    }

    @Override public int getItemCount() {
        return mFilteredList != null ? mFilteredList.size() : 0;
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public ModuleInfo getModuleInfoForPosition(int position) {
        return mFilteredList.get(position);
    }

    public void setOnModuleStateChangedListener(OnModuleStateChangedListener listener) {
        this.mModuleStateChangedListener = listener;
    }

    public synchronized void setModules(List<ModuleInfo> modules) {
        this.mSourceList = modules;
        this.mFilteredList = modules;
        notifyDataSetChanged();
    }

    @MainThread @Override public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ModulesFilter();
        }
        return mFilter;
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        TextView version;
        TextView error;
        ImageView icon;
        CheckBox checkbox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.module_name);
            description = itemView.findViewById(R.id.module_description);
            version = itemView.findViewById(R.id.module_version);
            error = itemView.findViewById(R.id.module_error);
            icon = itemView.findViewById(R.id.module_icon);
            checkbox = itemView.findViewById(R.id.module_checkbox);
        }
    }

    final class ModulesFilter extends Filter {
        @Override protected FilterResults performFiltering(CharSequence constraint) {
            List<ModuleInfo> sourceList;
            synchronized (ModuleListAdapter.this) {
                sourceList = mSourceList;
            }
            List<ModuleInfo> filteredList = new ArrayList<>();
            if (TextUtils.isEmpty(constraint)) {
                filteredList.addAll(sourceList);
            } else {
                String constraintStrLowerCase = constraint.toString().toLowerCase();
                for (ModuleInfo moduleInfo : sourceList) {
                    if (moduleInfo.name.toLowerCase().contains(constraintStrLowerCase)
                            || moduleInfo.packageName.toLowerCase().contains(constraintStrLowerCase)
                            || moduleInfo.description.toLowerCase().contains(constraintStrLowerCase)) {
                        filteredList.add(moduleInfo);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.count = filteredList.size();
            filterResults.values = filteredList;
            return filterResults;
        }

        @SuppressWarnings("unchecked") @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized (ModuleListAdapter.this) {
                mFilteredList = (List<ModuleInfo>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public interface OnModuleStateChangedListener {
        void onModuleStateChanged();
    }
}
