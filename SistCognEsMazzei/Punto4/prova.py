import sys

INPUT = sys.stdin

def do_something_with_data(line):
    return "merda"

def main():
    for line in INPUT:
        print 'Result:', do_something_with_data(line)

if __name__ == '__main__':
    main()