package com.meupersonal;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.meupersonal.presentation.auth.AuthViewModel;

public final class MainActivity  extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) throw new IllegalStateException("NavHostFragment não encontrado.");

        navController = navHost.getNavController();
        bottomNavigation = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        navController.addOnDestinationChangedListener(this::onDestinationChanged);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getSessionRestored().observe(this, restored -> {
            if (Boolean.TRUE.equals(restored)
                    && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.loginFragment) {
                navController.navigate(R.id.action_global_dashboard);
            }
        });
        if (savedInstanceState == null) authViewModel.restoreSession();
    }

    private void onDestinationChanged(NavController controller,
                                      NavDestination destination, Bundle arguments) {
        int id = destination.getId();
        boolean topLevel = id == R.id.dashboardFragment
                || id == R.id.listaTreinosFragment
                || id == R.id.especificacoesFragment;
        bottomNavigation.setVisibility(topLevel ? View.VISIBLE : View.GONE);
    }
}