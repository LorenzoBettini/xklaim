package xklaim.example.leaderelection;

import xklaim.example.leaderelection.LeaderElectionNet;

@SuppressWarnings("all")
public class LeaderElection {
  public static void main(final String[] args) throws Exception {
    LeaderElectionNet leaderElectionNet = new LeaderElectionNet();
    leaderElectionNet.addNodes();
  }
}
