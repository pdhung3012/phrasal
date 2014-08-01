from csv import DictReader
from collections import defaultdict
from itertools import combinations

"""
Responsible for reading in WMT-formatted CSV files, and outputting pairwise ranks.
"""

def get_pairranks(rankList):
    ### Reading a list of rank and return a list of pairwise comparison {>, <, =}.
    result = []
    for pair in combinations(rankList, 2):
        if pair[0] == pair[1]:
            result.append('=')
        elif pair[0] > pair[1]:
            result.append('>')
        else:
            result.append('<')
    return result

def get_pairwise(names, ranks):
    """Takes a ranking task (list of systems, list of ranks) and returns the set of pairwise rankings."""
    pairname = [n for n in combinations(names, 2)]
    pairwise = get_pairranks(ranks)
    pair_result = []
    for pn, pw in zip(pairname, pairwise):
        pair_result.append((pn[0], pn[1], pw))
    return pair_result

def rankings(fh):
    """Reads in a CSV file fh, returning each 5-way ranking."""

    ### Parsing csv file and return system names and rank(1-5) for each sentence
    sent_sys_rank = defaultdict(list)
    for i,row in enumerate(DictReader(fh)):
        sentID = int(row.get('segmentId'))
        systems = []
        ranks = []
        for num in range(1, 6):
            systems.append(row.get('system%dId' % num))
            ranks.append(int(row.get('system%drank' % num)))

        if not -1 in ranks:
            yield (systems, ranks, sentID)

def pairs(fh):
    """Reads in a CSV file fh, returning pairwise judgments."""
    for systems, ranks, sentID in rankings(fh):
        for pair in get_pairwise(systems, ranks):
            yield pair + (sentID,)
            
def numeric_observation(obs):
    if obs == '<':
        return 0
    elif obs == '=':
        return 1
    elif obs == '>':
        return 2

    raise RuntimeException()

def parse_csv(fh):
    ### Parsing csv file and return system names and rank(1-5) for each sentence
    all_systems = []
    sent_sys_rank = defaultdict(list)
    for i,row in enumerate(DictReader(fh)):
        sentID = int(row.get('segmentId'))
        systems = []
        ranks = []
        for num in range(1, 6):
            if row.get('system%dId' % num) in all_systems:
                pass
            else:
                all_systems.append(row.get('system%dId' % num))
            systems.append(row.get('system%dId' % num))
            ranks.append(int(row.get('system%drank' % num)))
        if -1 in ranks:
            pass
        else:
            sent_sys_rank[sentID].append({'systems': systems, 'ranks': ranks})
    return all_systems, sent_sys_rank
