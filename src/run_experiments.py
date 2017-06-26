import argparse
import sys
import os
import subprocess

def execute_one_experiment(executable, otn_topology_file, ip_topology_file,
                           ip_node_mapping_file, ip_link_mapping_file,
                           ip_port_info_file, vn_topology_file,
                           location_constraint_file, num_shuffles):
    process = subprocess.Popen([executable, "--otn_topology_file=" + otn_topology_file,
            "--ip_topology_file=" + ip_topology_file, 
            "--ip_node_mapping_file=" + ip_node_mapping_file,
            "--ip_link_mapping_file=" + ip_link_mapping_file,
            "--ip_port_info_file=" + ip_port_info_file,
            "--vn_topology_file=" + vn_topology_file,
            "--vn_location_file=" + location_constraint_file,
            "--num_shuffles=" + str(num_shuffles)],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=False)
    out, err = process.communicate()
    with open(vn_topology_file + ".stdout", "w") as f:
        f.write(out)
    with open(vn_topology_file + ".stderr", "w") as f:
        f.write(err)
    if os.path.isfile(vn_topology_file + ".status"):
        with open(vn_topology_file + ".status") as f:
            print vn_topology_file + ": " + f.readline()

def main():
    parser = argparse.ArgumentParser(
        description="Script for automating Multilayer VNE experiments",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument(
        '--testcase_root',
        help='Root directory for test cases',
        required=True)
    parser.add_argument(
        '--executable',
        help='Name of the executable file to run',
        required=True)
    parser.add_argument(
        '--num_shuffles',
        help='Number of times to shuffle the vnode ordering',
        required=False)
    args = parser.parse_args()
    root = args.testcase_root
    executable = './' + args.executable
    subprocess.Popen(['make'], shell=False, stdout=subprocess.PIPE,
                     stderr=subprocess.PIPE)
    otn_topology_file = args.testcase_root + "/otn.topo"
    ip_topology_file = args.testcase_root + "/ip.topo"
    ip_node_mapping_file = args.testcase_root + "/ip.nmap"
    ip_link_mapping_file = args.testcase_root + "/ip.emap"
    ip_port_info_file = args.testcase_root + "/ip-port"
    i = 0
    while True:
        vn_topology_file = os.path.join(args.testcase_root, "vn" + str(i) + ".topo")
        location_constraint_file = os.path.join(args.testcase_root, "vnloc" + str(i))
        if not os.path.isfile(vn_topology_file):
            break
        best_cost = 99999999
        for trial in range(0, 5):
            execute_one_experiment(executable, otn_topology_file, ip_topology_file,
                                   ip_node_mapping_file, ip_link_mapping_file,
                                   ip_port_info_file, vn_topology_file,
                                   location_constraint_file, args.num_shuffles)
            with open(vn_topology_file + ".status", "r") as f:
                line = f.readline()
                if line.strip("\r\n") == "Success":
                    costf = open(vn_topology_file + ".cost")
                    cost = int(costf.readline().strip("\r\n"))
                    if cost < best_cost:
                        best_cost = cost
                    costf.close()
            if best_cost <> 99999999:
                f = open(vn_topology_file + ".cost", "w")
                f.write(str(best_cost) + "\n")
        i = i + 1
if __name__ == "__main__":
    main()
