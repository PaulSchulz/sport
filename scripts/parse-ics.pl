#!/usr/bin/perl -w

# This script takes an ICS (ical) file and converts it into a YAML formatted
# file. It is ment to be only RUN ONCE, in order to generate data for an initial
# import.

# This is a working script and need to be modified before use.
# It was initially created for WBBL 2020

# Special Treatments
# ------------------
# 2021-afl
# - SUMMARY: Team names and round number extracted from SUMMARY
#   also contains an extra '^M' at the end (maybe they all do?)
# - LOCATION contains an extra '^M' at the end of the line for some reason.
#   Fix by removing an extra character from end of the line during parsing.

$debug = 0;

$infile = "../data/download/afl-2021-UTC.ics";
%header = (
    "title" =>     "2021 AFL",
    "location" => "Australia",
    "code" =>     "afl",
    "date" =>     "",
    "url" =>      "",
    "version" =>  "1.0",
    "name" =>     "afl-2021",
    "filename" => "data/2021-aus-afl"
    );

my %teams;
my %venues;

my $id = 0;

print "---\n";
for $key (keys(%header)) {
    print "$key:     \"$header{$key}\"\n"
}

print "\n";
print "games:\n";
open(DATA, "<$infile") or die "Couldn't open file: $infile";
while($line = <DATA>){
    chomp $line;
    # print "$line";

    if ($debug) { print "# $line\n"; }

    if ($line =~ /^BEGIN:VCALENDAR/) {
    } elsif ($line =~ /^END:VCALENDAR/) {
        print "\n";
        print "teams:\n";
        for $key (keys(%teams)) {
            print "  - { name: \"$key\" }\n"
        }
        print "\n";
        print "venues:\n";
        for $key (keys(%venues)) {
            print "  - $key\n"
        }
    } elsif ($line =~ /^PRODID:/) {
    } elsif ($line =~ /^VERSION:/) {
    } elsif ($line =~ /^METHOD:/) {
    } elsif ($line =~ /^BEGIN:VEVENT/) {
    } elsif ($line =~ /^END:VEVENT/) {
        $id++;
        print "  - game:        $id\n";
        print "    datetime:    \"$datetime\"\n";
        print "    round:       $round\n";
        print "    home:        \"$home\"\n";
        print "    away:        \"$away\"\n";
        print "    score:       {}\n";
        print "    result:      {}\n";
        print "    venue:       \"$venue\"\n";
        print "    description: \"$description\"\n";
        print "    summary:     \"$summary\"\n";
        print "\n";
    } elsif ($line =~ /^DESCRIPTION:/) {
        ($description) = ($line =~ /^DESCRIPTION:(.*).$/);
    } elsif ($line =~ /^SUMMARY:/) {
        ($summary) = ($line =~ /^SUMMARY:(.*).$/);
        ($home, $away, $round) =  ($line =~ /^SUMMARY:(.*) vs (.*) - .* Round (\d+)/);
        $teams{$home} = "";
        $teams{$away} = "";

    } elsif ($line =~ /^SUMMARY:Final /) {
        # Never get called
        ($summary) = ($line =~ /^SUMMARY:(.*)$/);
        ($match, $home, $away) =  ($line =~ /^SUMMARY:(Final) (.+) v (.+)$/);
        $teams{$home} = "";
        $teams{$away} = "";

    } elsif ($line =~ s/^DTSTART://) {
        ($year, $month, $day, $hour, $min, $sec)
            = ($line =~ /(....)(..)(..)T(..)(..)(..)Z/);
        $datetime = "$day/$month/$year $hour:$min";
    } elsif ($line =~ /DTEND:/) {
    } elsif ($line =~ /^LOCATION:/) {
        # Location contains an extra '^M' at the end of the line for some reason.
        # Fix by removing an extra character from end of the line.
        ($venue) = ($line =~ /^LOCATION:(.*).$/);
        $venues{$venue} = "";
    } elsif ($line =~ /^TRANSP:/) {
    } elsif ($line =~ /^UID:/) {
    } elsif ($line =~ /^BEGIN:VALARM/) {
    } elsif ($line =~ /^TRIGGER:/) {
    } elsif ($line =~ /^ACTION:/) {
    } elsif ($line =~ /^END:VALARM/) {
    }
}
