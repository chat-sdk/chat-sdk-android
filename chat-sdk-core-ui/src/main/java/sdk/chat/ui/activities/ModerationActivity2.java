package sdk.chat.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import smartadapter.SmartRecyclerAdapter;

public class ModerationActivity2 extends BaseActivity {

    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    protected SmartRecyclerAdapter adapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_moderation;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List items = new ArrayList<>();
//        adapter = new SmartRecyclerAdapter(items).map(getKClass(Integer.class), getKClass(SelectableViewHolder.Radio.class));


    }

    public static <T> KClass<T> getKClass(Class<T> cls){
        return JvmClassMappingKt.getKotlinClass(cls);
    }
}
