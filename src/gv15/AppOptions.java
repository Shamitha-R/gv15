package gv15;

/**
 *
 * @author ranasi01
 */
public class AppOptions {
    public String OptionsList = "Usage: gv15.jar [-options]\n"
            + "Available Options\n"
            + " -path={PATH}                    Define the path of a parameters file (prefs.txt). Other options are ignored when the path is defined.\n"
            + " -datapath={val}                 Path of the target BAM and BAI files.\n"
            + " -referencepath={val}            Path of the Reference .FASTA file.\n"
            + " -variantpath={val}              Path of Variants .vcf file.\n"
            + " -phenotypepath={val}            Path of the Phenotypes .csv file.\n"
            + " -cachepath={val}                Path to dump application cache.\n"
            + " -outputpath={val}               Path to dump rendered images.\n"
            + " -width={val}                    Width of the rendered image.\n"
            + " -height={val}                   Height of the rendered image.\n"
            + " -flank={val}                    Number of base pairs to left/right of variant.\n"
            + " -gridstartx={val}               X coordinate of the first panel.\n"
            + " -gridstarty={val}               Y coordinate of the first panel.\n"
            + " -panelseparation={val}          Seperation between panels in pixels.\n"
            + " -columns={val}                  Number of columns to render in each panel.\n"
            + " -columnwidth={val}              Width of each individual column in all panels.\n"
            + " -rowheight={val}                Height of each individual row in all panels.\n"
            + " -fragmentxoffset={val}          Padding applied between fragments.\n"
            + " -outputtype={val}               Output type of rendered images. Supports png/jpeg.\n"
            + " -phenotypecolumn={val}          Target phenotype column in the phenotypes file.\n"
            + " -readcountrenderthreshold={val} Minimum read count to render fragment.\n"
            + " -insertionsonlyatvariant={val}  Show all insertions 0.Show insertions only at variant 1.\n"
            + " -readcolour_unvaried={val}      HEX colour of unvaried fragments.\n"
            + " -readcolour_varied={val}        HEX colour of varies fragments.\n"
            + " -readcolour_insertion={val}     HEX colour of insertion fragments.\n";
}
