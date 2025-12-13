import '@mui/material';

declare module '@mui/material/Grid' {
  interface GridProps {
    item?: boolean;
    xs?: number | 'auto';
    sm?: number | 'auto';
    md?: number | 'auto';
    lg?: number | 'auto';
    xl?: number | 'auto';
  }
}

