package jalview.datamodel.xdb.embl;

public class EmblFeatureLocElement {
        String type;
        String accession;
        String version;
        boolean complement;
        BasePosition basePositions[]; 
        /**
         * @return the accession
         */
        public String getAccession() {
            return accession;
        }
        /**
         * @param accession the accession to set
         */
        public void setAccession(String accession) {
            this.accession = accession;
        }
        /**
         * @return the basePositions
         */
        public BasePosition[] getBasePositions() {
            return basePositions;
        }
        /**
         * @param basePositions the basePositions to set
         */
        public void setBasePositions(BasePosition[] basePositions) {
            this.basePositions = basePositions;
        }
        /**
         * @return the complement
         */
        public boolean isComplement() {
            return complement;
        }
        /**
         * @param complement the complement to set
         */
        public void setComplement(boolean complement) {
            this.complement = complement;
        }
        /**
         * @return the type
         */
        public String getType() {
            return type;
        }
        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }
        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }
        /**
         * @param version the version to set
         */
        public void setVersion(String version) {
            this.version = version;
        }
    }
    
