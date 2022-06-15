package adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.falldetectionsystem.PatientListActivity;
import com.example.falldetectionsystem.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import helper.User;

public class PatientRecyclerAdapter extends RecyclerView.Adapter<PatientRecyclerAdapter.UserViewHolder> {

    private List<User> patientList;
    private RecyclerViewClickListener recyclerViewClickListener;

    public PatientRecyclerAdapter(List<User> patientList, RecyclerViewClickListener recyclerViewClickListener) {
        this.patientList = patientList;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_patient_recycler, parent, false);

        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.usernameTv.setText(patientList.get(position).getName());
        holder.patientNameTv.setText(patientList.get(position).getPatientname());
        String address = patientList.get(position).getStreet()+", "+ patientList.get(position).getCity()
                +",\n"+patientList.get(position).getCountry()+", "+patientList.get(position).getPostalCode();
        holder.addressTv.setText(address);
    }

    @Override
    public int getItemCount() {
        Log.d(PatientListActivity.class.getSimpleName(), String.valueOf(patientList.size()));
        return patientList.size();
    }

    public interface RecyclerViewClickListener{
        void OnClick(View v, int position);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView usernameTv;
        public TextView patientNameTv;
        public TextView addressTv;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.item_user_name_tv);
            patientNameTv = itemView.findViewById(R.id.item_patient_name_tv);
            addressTv = itemView.findViewById(R.id.item_address_tv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recyclerViewClickListener.OnClick(v,getAdapterPosition());
        }
    }
}
