package xklaim.example.leaderelectionrec;

@SuppressWarnings("all")
public class LeaderElectionRecursiveMain {
  public static void main(final String[] args) throws Exception {
    LeaderElectionRecursiveNet leaderElectionRecursiveNet = new LeaderElectionRecursiveNet();
    leaderElectionRecursiveNet.addNodes();
    leaderElectionRecursiveNet.waitForCompletion();
  }
}
