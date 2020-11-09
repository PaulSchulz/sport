#!/usr/bin/perl -w

# This script takes an ICS (ical) file and converts it into a YAML formatted
# file. It is ment to be only RUN ONCE, in order to generate data for an initial
# import.

# This is a working script and need to be modified before use.
# It was initially created for WBBL 2020

$infile = "../data/download/Women's_Big_Bash_League.ics";
%header = (
    "title" =>     "2020 WBBL",
    "location" => "Australia",
    "code" =>     "bbl",
    "date" =>     "",
    "url" =>      "",
    "version" =>  "1.0",
    "name" =>     "wbbl-2020",
    "filename" => "data/2020-aus-wbbl.yml"
    );

my %teams;
my %venues;

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

    if ($line =~ /^BEGIN:VCALENDAR/) {
    } elsif ($line =~ /^END:VCALENDAR/) {
        print "\n";
        print "teams:\n";
        for $key (keys(%teams)) {
            print "  - {name: \"$key\"}\n"
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
        print "    score:    {}\n";
        print "    result:   {}\n";
        print "    summary:  \"\"\n";
        print "\n";
    } elsif ($line =~ /^SUMMARY:(\d+).. \S+/) {
        ($id, $match, $home, $away) =  ($line =~ /^SUMMARY:(\d+).. (\S+) (.+) v (.+)$/);
        $match = lc($match);
        print "  - game:     \"$match-$id\"\n";
        print "    home:     \"$home\"\n";
        print "    away:     \"$away\"\n";

        $teams{$home} = "";
        $teams{$away} = "";

    } elsif ($line =~ /^SUMMARY:Final /) {
        ($match, $home, $away) =  ($line =~ /^SUMMARY:(Final) (.+) v (.+)$/);
        $match = lc($match);
        print "  - game:     \"$match\"\n";
        print "    home:     \"$home\"\n";
        print "    away:     \"$away\"\n";

        $teams{$home} = "";
        $teams{$away} = "";

    } elsif ($line =~ s/^DTSTART://) {
        ($year, $month, $day, $hour, $min, $sec)
            = ($line =~ /(....)(..)(..)T(..)(..)(..)Z/);
        print "    datetime: \"$day/$month/$year $hour:$sec\"\n";
    } elsif ($line =~ /DTEND:/) {
    } elsif ($line =~ s/^LOCATION://) {
        print "    venue:    \"$line\"\n";

        $venues{$line} = "";

    } elsif ($line =~ /^DESCRIPTION:/) {
    } elsif ($line =~ /^TRANSP:/) {
    } elsif ($line =~ /^UID:/) {
    } elsif ($line =~ /^BEGIN:VALARM/) {
    } elsif ($line =~ /^TRIGGER:/) {
    } elsif ($line =~ /^ACTION:/) {
    } elsif ($line =~ /^END:VALARM/) {
    }  else {
        print "# $line\n";
    }
}
