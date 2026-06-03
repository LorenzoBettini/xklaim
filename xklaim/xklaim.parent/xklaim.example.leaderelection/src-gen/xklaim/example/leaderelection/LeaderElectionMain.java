package xklaim.example.leaderelection;

@SuppressWarnings("all")
public class LeaderElectionMain {
  public static void main(final String[] args) throws Exception {
    LeaderElectionNet leaderElectionNet = new LeaderElectionNet();
    leaderElectionNet.addNodes();
    leaderElectionNet.waitForCompletion();
  }
}
