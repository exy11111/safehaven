package com.xql.safehaven;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.xql.safehaven.databinding.CardHistoryBinding;
import com.xql.safehaven.databinding.FragmentHistoryBinding;
import com.xql.safehaven.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    FragmentHistoryBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String userId;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(currentUser == null){
            Intent intent = new Intent(requireContext(), HomepageActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
        else{
            userId = currentUser.getUid();
        }

        db.collection("logs").orderBy("datetime", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                for (DocumentSnapshot document : querySnapshot) {

                    String category = document.getString("category");

                    if(category != null && category.equals("rest")){
                        String restStatus = document.getString("restStatus");
                        String restNote = document.getString("restNote");
                        String logId = document.getString("userId");
                        Timestamp datetime = document.getTimestamp("datetime");
                        addRestLogCardToLayout(restStatus, restNote, datetime, logId);
                    }

                }
            }
        });

        return binding.getRoot();
    }

    private void addRestLogCardToLayout(String restStatus, String restNote, Timestamp datetime, String logId) {
        CardHistoryBinding cardBinding = CardHistoryBinding.inflate(LayoutInflater.from(getContext()), binding.parentLayout, false);
        int rosyRed = ContextCompat.getColor(requireContext(), R.color.rosyRed);
        int success = ContextCompat.getColor(requireContext(), R.color.success);

        cardBinding.category.setImageResource(R.drawable.bed_clock);
        cardBinding.categoryLabel.setText("Resting status: ");
        cardBinding.categoryValue.setText(restStatus);
        if(restStatus.equals("Awake")){
            cardBinding.categoryValue.setTextColor(success);
        }
        else{
            cardBinding.categoryValue.setTextColor(rosyRed);
        }

        if(logId.equals(userId)){
            cardBinding.user.setText("You");
        }
        else{
            cardBinding.user.setText("Babi");
        }

        cardBinding.note.setText("\""+restNote+"\"");

        Date date = datetime.toDate();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
        String formattedDate = formatter.format(date).toLowerCase();
        String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        cardBinding.datetime.setText(capitalizedDate);

        binding.parentLayout.addView(cardBinding.getRoot());
    }
}